import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

public class Matricula implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int CAPACIDADE_MAXIMA = 60;
    private static final FileStorage<Matricula> STORAGE =
            new FileStorage<>(Path.of("dados", "matriculas.bin"), Matricula.class);
    private static final FileStorage<Usuario> USUARIOS =
            new FileStorage<>(Path.of("dados", "usuarios.bin"), Usuario.class);
    private static final FileStorage<Turma> TURMAS =
            new FileStorage<>(Path.of("dados", "turmas.bin"), Turma.class);
    private static final FileStorage<Disciplina> DISCIPLINAS =
            new FileStorage<>(Path.of("dados", "disciplinas.bin"), Disciplina.class);

    private final String alunoMatricula;
    private final String turmaCodigo;
    private final String semestre;
    private StatusMatricula status;

    public Matricula(String alunoMatricula, String turmaCodigo, String semestre) {
        if (alunoMatricula == null || alunoMatricula.isBlank() || turmaCodigo == null || turmaCodigo.isBlank()
                || semestre == null || semestre.isBlank()) {
            throw new IllegalArgumentException("Aluno, turma e semestre são obrigatórios.");
        }
        this.alunoMatricula = alunoMatricula;
        this.turmaCodigo = turmaCodigo;
        this.semestre = semestre;
        this.status = StatusMatricula.PENDENTE;
    }

    public boolean confirmar() throws IOException {
        if (status != StatusMatricula.PENDENTE || !JanelaMatricula.estaAberto(semestre)) {
            return false;
        }

        Aluno aluno = USUARIOS.readAll().stream()
                .filter(usuario -> usuario.getMatricula().equals(alunoMatricula))
                .filter(Aluno.class::isInstance)
                .map(Aluno.class::cast)
                .findFirst().orElse(null);
        Turma turma = TURMAS.readAll().stream()
                .filter(item -> item.getCodigo().equals(turmaCodigo))
                .findFirst().orElse(null);
        Disciplina disciplina = DISCIPLINAS.readAll().stream()
                .filter(item -> turma != null && item.getCodigo().equals(turma.getDisciplinaCodigo()))
                .findFirst().orElse(null);

        if (aluno == null || aluno.getStatus() != StatusAluno.ATIVO || turma == null
                || turma.getStatus() != StatusTurma.ABERTA || !turma.getSemestre().equals(semestre)
                || disciplina == null || contarAtivas(turmaCodigo) >= CAPACIDADE_MAXIMA) {
            return false;
        }

        List<Matricula> matriculas = STORAGE.readAll();
        boolean jaMatriculado = matriculas.stream().anyMatch(matricula ->
                matricula.alunoMatricula.equals(alunoMatricula)
                        && matricula.turmaCodigo.equals(turmaCodigo)
                        && matricula.semestre.equals(semestre)
                        && matricula.status == StatusMatricula.CONFIRMADA);
        if (jaMatriculado || !respeitaLimiteDeOpcoes(matriculas, disciplina.getCodigo(), disciplina.getTipo())) {
            return false;
        }

        status = StatusMatricula.CONFIRMADA;
        matriculas.add(this);
        STORAGE.writeAll(matriculas);
        NotificacaoCobranca.enfileirar(alunoMatricula, turmaCodigo, semestre);
        if (contarAtivas(turmaCodigo) >= CAPACIDADE_MAXIMA) {
            turma.definirStatus(StatusTurma.ENCERRADA);
            turma.salvar();
        }
        return true;
    }

    public boolean cancelar() throws IOException {
        if (!JanelaMatricula.estaAberto(semestre)) {
            return false;
        }

        List<Matricula> matriculas = STORAGE.readAll();
        Matricula salva = matriculas.stream()
                .filter(item -> item.alunoMatricula.equals(alunoMatricula)
                        && item.turmaCodigo.equals(turmaCodigo)
                        && item.semestre.equals(semestre)
                        && item.status == StatusMatricula.CONFIRMADA)
                .findFirst().orElse(null);
        if (salva == null) {
            return false;
        }
        salva.status = StatusMatricula.CANCELADA;
        status = StatusMatricula.CANCELADA;
        STORAGE.writeAll(matriculas);

        Turma turma = TURMAS.readAll().stream()
                .filter(item -> item.getCodigo().equals(turmaCodigo))
                .findFirst().orElse(null);
        if (turma != null && turma.getStatus() == StatusTurma.ENCERRADA
                && contarAtivas(turmaCodigo) < CAPACIDADE_MAXIMA) {
            turma.definirStatus(StatusTurma.ABERTA);
            turma.salvar();
        }
        return true;
    }

    private boolean respeitaLimiteDeOpcoes(List<Matricula> matriculas, String novaDisciplinaCodigo,
            TipoDisciplina novoTipo)
            throws IOException {
        long obrigatorias = 0;
        long optativas = 0;
        java.util.Set<String> disciplinasMatriculadas = new java.util.HashSet<>();
        List<Turma> turmas = TURMAS.readAll();
        List<Disciplina> disciplinas = DISCIPLINAS.readAll();

        for (Matricula matricula : matriculas) {
            if (!matricula.alunoMatricula.equals(alunoMatricula) || !matricula.semestre.equals(semestre)
                    || matricula.status != StatusMatricula.CONFIRMADA) {
                continue;
            }
            Turma turma = turmas.stream()
                    .filter(item -> item.getCodigo().equals(matricula.turmaCodigo))
                    .findFirst().orElse(null);
            if (turma == null) {
                continue;
            }
            Disciplina disciplina = disciplinas.stream()
                    .filter(item -> item.getCodigo().equals(turma.getDisciplinaCodigo()))
                    .findFirst()
                    .orElse(null);
            if (disciplina != null && disciplina.getTipo() == TipoDisciplina.OBRIGATORIA) {
                disciplinasMatriculadas.add(disciplina.getCodigo());
                obrigatorias++;
            } else if (disciplina != null) {
                disciplinasMatriculadas.add(disciplina.getCodigo());
                optativas++;
            }
        }

        if (disciplinasMatriculadas.contains(novaDisciplinaCodigo)) {
            return false;
        }
        return novoTipo == TipoDisciplina.OBRIGATORIA ? obrigatorias < 1 : optativas < 2;
    }

    public static int contarAtivas(String turmaCodigo) throws IOException {
        List<Usuario> usuarios = USUARIOS.readAll();
        return (int) STORAGE.readAll().stream()
                .filter(matricula -> matricula.turmaCodigo.equals(turmaCodigo)
                        && matricula.status == StatusMatricula.CONFIRMADA)
                .filter(matricula -> usuarios.stream().anyMatch(usuario ->
                        usuario.getMatricula().equals(matricula.alunoMatricula)
                                && usuario instanceof Aluno aluno && aluno.getStatus() == StatusAluno.ATIVO))
                .count();
    }

    static void cancelarDaTurma(String turmaCodigo) throws IOException {
        List<Matricula> matriculas = STORAGE.readAll();
        matriculas.stream()
                .filter(matricula -> matricula.turmaCodigo.equals(turmaCodigo)
                        && matricula.status == StatusMatricula.CONFIRMADA)
                .forEach(matricula -> matricula.status = StatusMatricula.CANCELADA);
        STORAGE.writeAll(matriculas);
    }

    static void concluirDaTurma(String turmaCodigo) throws IOException {
        List<Matricula> matriculas = STORAGE.readAll();
        matriculas.stream()
                .filter(matricula -> matricula.turmaCodigo.equals(turmaCodigo)
                        && matricula.status == StatusMatricula.CONFIRMADA)
                .forEach(matricula -> matricula.status = StatusMatricula.CONCLUIDA);
        STORAGE.writeAll(matriculas);
    }

    public String getAlunoMatricula() {
        return alunoMatricula;
    }

    public String getTurmaCodigo() {
        return turmaCodigo;
    }

    public String getSemestre() {
        return semestre;
    }

    public StatusMatricula getStatus() {
        return status;
    }
}

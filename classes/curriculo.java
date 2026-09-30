import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Curriculo implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final FileStorage<Curriculo> STORAGE =
            new FileStorage<>(Path.of("dados", "curriculos.bin"), Curriculo.class);

    public final String semestre;
    private List<String> alunos = new ArrayList<>();
    private List<String> disciplinas = new ArrayList<>();
    private List<String> professores = new ArrayList<>();

    public Curriculo(String semestre) {
        if (semestre == null || semestre.isBlank()) {
            throw new IllegalArgumentException("Semestre do currículo é obrigatório.");
        }
        this.semestre = semestre;
    }

    public void salvar() throws IOException {
        alunos = List.of(consultarAlunos());
        disciplinas = List.of(consultarDisciplinas());
        professores = List.of(consultarProfessores());
        List<Curriculo> curriculos = STORAGE.readAll();
        curriculos.removeIf(curriculo -> curriculo.semestre.equals(semestre));
        curriculos.add(this);
        STORAGE.writeAll(curriculos);
    }

    public String[] consultarAlunos() throws IOException {
        List<Usuario> usuarios = new FileStorage<Usuario>(Path.of("dados", "usuarios.bin"), Usuario.class)
                .readAll();
        List<Matricula> matriculas = new FileStorage<Matricula>(Path.of("dados", "matriculas.bin"), Matricula.class)
                .readAll();
        Set<String> alunos = new LinkedHashSet<>();

        for (Matricula matricula : matriculas) {
            if (!matricula.getSemestre().equals(semestre)
                    || matricula.getStatus() != StatusMatricula.CONFIRMADA) {
                continue;
            }
            usuarios.stream()
                    .filter(usuario -> usuario.getMatricula().equals(matricula.getAlunoMatricula()))
                    .filter(Aluno.class::isInstance)
                    .map(Aluno.class::cast)
                    .filter(aluno -> aluno.getStatus() == StatusAluno.ATIVO)
                    .map(aluno -> aluno.getMatricula() + " - " + aluno.getNome())
                    .forEach(alunos::add);
        }
        return alunos.toArray(String[]::new);
    }

    public String[] consultarDisciplinas() throws IOException {
        List<Turma> turmas = new FileStorage<Turma>(Path.of("dados", "turmas.bin"), Turma.class).readAll();
        List<Disciplina> disciplinas = new FileStorage<Disciplina>(Path.of("dados", "disciplinas.bin"),
                Disciplina.class).readAll();
        List<String> resultado = new ArrayList<>();

        for (Turma turma : turmas) {
            if (!turma.getSemestre().equals(semestre) || turma.getStatus() == StatusTurma.CANCELADA) {
                continue;
            }
            disciplinas.stream()
                    .filter(disciplina -> disciplina.getCodigo().equals(turma.getDisciplinaCodigo()))
                    .findFirst()
                    .ifPresent(disciplina -> resultado.add(disciplina.getCodigo() + " - " + disciplina.getNome()
                            + " (turma " + turma.getCodigo() + ")"));
        }
        return resultado.toArray(String[]::new);
    }

    public String[] consultarProfessores() throws IOException {
        List<Turma> turmas = new FileStorage<Turma>(Path.of("dados", "turmas.bin"), Turma.class).readAll();
        List<Usuario> usuarios = new FileStorage<Usuario>(Path.of("dados", "usuarios.bin"), Usuario.class)
                .readAll();
        Set<String> professores = new LinkedHashSet<>();

        for (Turma turma : turmas) {
            if (!turma.getSemestre().equals(semestre) || turma.getStatus() == StatusTurma.CANCELADA) {
                continue;
            }
            usuarios.stream()
                    .filter(usuario -> usuario.getMatricula().equals(turma.getProfessorMatricula()))
                    .filter(Professor.class::isInstance)
                    .map(Professor.class::cast)
                    .map(professor -> professor.getMatricula() + " - " + professor.getNome())
                    .forEach(professores::add);
        }
        return professores.toArray(String[]::new);
    }
}

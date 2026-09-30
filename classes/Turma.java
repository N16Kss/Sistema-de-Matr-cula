import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

public class Turma implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int CAPACIDADE_MAXIMA = 60;
    private static final int MINIMO_ALUNOS = 3;
    private static final FileStorage<Turma> STORAGE =
            new FileStorage<>(Path.of("dados", "turmas.bin"), Turma.class);

    private final String codigo;
    private final String disciplinaCodigo;
    private final String professorMatricula;
    private final String semestre;
    private StatusTurma status;

    public Turma(String codigo, String disciplinaCodigo, String professorMatricula, String semestre) {
        if (codigo == null || codigo.isBlank() || disciplinaCodigo == null || disciplinaCodigo.isBlank()
                || professorMatricula == null || professorMatricula.isBlank()
                || semestre == null || semestre.isBlank()) {
            throw new IllegalArgumentException("Turma, disciplina, professor e semestre são obrigatórios.");
        }
        this.codigo = codigo;
        this.disciplinaCodigo = disciplinaCodigo;
        this.professorMatricula = professorMatricula;
        this.semestre = semestre;
        this.status = StatusTurma.ABERTA;
    }

    public void salvar() throws IOException {
        List<Turma> turmas = STORAGE.readAll();
        turmas.removeIf(turma -> turma.codigo.equals(codigo));
        turmas.add(this);
        STORAGE.writeAll(turmas);
    }

    public boolean cancelarTurma() throws IOException {
        if (status != StatusTurma.ABERTA || JanelaMatricula.estaAberto(semestre)
                || Matricula.contarAtivas(codigo) >= MINIMO_ALUNOS) {
            return false;
        }
        status = StatusTurma.CANCELADA;
        salvar();
        Matricula.cancelarDaTurma(codigo);
        return true;
    }

    public boolean encerrarInscricoes() throws IOException {
        int matriculados = Matricula.contarAtivas(codigo);
        if (matriculados < CAPACIDADE_MAXIMA || status != StatusTurma.ABERTA) {
            return false;
        }
        status = StatusTurma.ENCERRADA;
        salvar();
        return true;
    }

    public boolean concluirTurma() throws IOException {
        if (status == StatusTurma.CANCELADA) {
            return false;
        }
        status = StatusTurma.ENCERRADA;
        salvar();
        Matricula.concluirDaTurma(codigo);
        return true;
    }

    void definirStatus(StatusTurma status) {
        this.status = status;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDisciplinaCodigo() {
        return disciplinaCodigo;
    }

    public String getProfessorMatricula() {
        return professorMatricula;
    }

    public String getSemestre() {
        return semestre;
    }

    public StatusTurma getStatus() {
        return status;
    }
}

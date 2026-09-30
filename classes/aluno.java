import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Aluno extends Usuario {
    private StatusAluno status;
    private String nome;

    public Aluno(String matricula, String senha, String nome, StatusAluno status) {
        super(matricula, senha);
        if (nome == null || nome.isBlank() || status == null) {
            throw new IllegalArgumentException("Nome e status do aluno são obrigatórios.");
        }
        this.nome = nome;
        this.status = status;
    }

    public boolean atualizarDados(String nome, StatusAluno status) throws IOException {
        if (nome == null || nome.isBlank() || status == null) {
            return false;
        }
        this.nome = nome;
        this.status = status;
        salvarUsuario();
        return true;
    }

    public boolean matricular(Turma turma, String semestre) throws IOException {
        if (status != StatusAluno.ATIVO || turma == null || !turma.getSemestre().equals(semestre)) {
            return false;
        }
        return new Matricula(getMatricula(), turma.getCodigo(), semestre).confirmar();
    }

    public boolean cancelarMatricula(String turmaCodigo, String semestre) throws IOException {
        if (status != StatusAluno.ATIVO) {
            return false;
        }
        return new Matricula(getMatricula(), turmaCodigo, semestre).cancelar();
    }

    public List<String> consultarHistorico() throws IOException {
        if (status != StatusAluno.ATIVO) {
            return List.of();
        }
        List<String> historico = new ArrayList<>();
        List<Matricula> matriculas = new FileStorage<>(Path.of("dados", "matriculas.bin"), Matricula.class)
                .readAll();
        List<Turma> turmas = new FileStorage<>(Path.of("dados", "turmas.bin"), Turma.class).readAll();
        List<Disciplina> disciplinas = new FileStorage<>(Path.of("dados", "disciplinas.bin"), Disciplina.class)
                .readAll();

        for (Matricula matricula : matriculas) {
            if (matricula.getAlunoMatricula().equals(getMatricula())
                    && matricula.getStatus() == StatusMatricula.CONCLUIDA) {
                Turma turma = turmas.stream()
                        .filter(item -> item.getCodigo().equals(matricula.getTurmaCodigo()))
                        .findFirst().orElse(null);
                if (turma != null) {
                    disciplinas.stream()
                            .filter(item -> item.getCodigo().equals(turma.getDisciplinaCodigo()))
                            .findFirst()
                            .ifPresent(disciplina -> historico.add(disciplina.getCodigo() + " - "
                                    + disciplina.getNome() + " (turma " + turma.getCodigo() + ")"));
                }
            }
        }
        return historico;
    }

    public String getNome() {
        return nome;
    }

    public StatusAluno getStatus() {
        return status;
    }
}

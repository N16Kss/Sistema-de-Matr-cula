import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Professor extends Usuario {
    private String nome;

    public Professor(String matricula, String senha, String nome) {
        super(matricula, senha);
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do professor é obrigatório.");
        }
        this.nome = nome;
    }

    public String consultarAlunos() throws IOException {
        List<Usuario> usuarios = new FileStorage<Usuario>(Path.of("dados", "usuarios.bin"), Usuario.class)
                .readAll();
        List<Turma> turmas = new FileStorage<Turma>(Path.of("dados", "turmas.bin"), Turma.class)
                .readAll();
        List<Matricula> matriculas = new FileStorage<Matricula>(Path.of("dados", "matriculas.bin"), Matricula.class)
                .readAll();
        Set<String> alunos = new LinkedHashSet<>();

        for (Turma turma : turmas) {
            if (!turma.getProfessorMatricula().equals(getMatricula())) {
                continue;
            }
            for (Matricula matricula : matriculas) {
                if (matricula.getTurmaCodigo().equals(turma.getCodigo())
                        && matricula.getStatus() == StatusMatricula.CONFIRMADA) {
                    usuarios.stream()
                            .filter(usuario -> usuario.getMatricula().equals(matricula.getAlunoMatricula()))
                            .filter(Aluno.class::isInstance)
                            .map(Aluno.class::cast)
                            .filter(aluno -> aluno.getStatus() == StatusAluno.ATIVO)
                            .map(Aluno::getNome)
                            .forEach(alunos::add);
                }
            }
        }
        return String.join(System.lineSeparator(), alunos);
    }

    public int consultarQuantidadeAlunos(String turmaCodigo) throws IOException {
        boolean turmaDoProfessor = new FileStorage<Turma>(Path.of("dados", "turmas.bin"), Turma.class)
                .readAll().stream()
                .anyMatch(turma -> turma.getCodigo().equals(turmaCodigo)
                        && turma.getProfessorMatricula().equals(getMatricula()));
        if (!turmaDoProfessor) {
            throw new IllegalArgumentException("A turma não pertence a este professor.");
        }
        return Matricula.contarAtivas(turmaCodigo);
    }

    public List<String> consultarAlunos(String turmaCodigo) throws IOException {
        boolean turmaDoProfessor = new FileStorage<Turma>(Path.of("dados", "turmas.bin"), Turma.class)
                .readAll().stream()
                .anyMatch(turma -> turma.getCodigo().equals(turmaCodigo)
                        && turma.getProfessorMatricula().equals(getMatricula()));
        if (!turmaDoProfessor) {
            throw new IllegalArgumentException("A turma não pertence a este professor.");
        }

        List<Usuario> usuarios = new FileStorage<Usuario>(Path.of("dados", "usuarios.bin"), Usuario.class)
                .readAll();
        List<Matricula> matriculas = new FileStorage<Matricula>(Path.of("dados", "matriculas.bin"), Matricula.class)
                .readAll();
        List<String> alunos = new ArrayList<>();
        for (Matricula matricula : matriculas) {
            if (!matricula.getTurmaCodigo().equals(turmaCodigo)
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
        return alunos;
    }

    public boolean atualizarDados(String nome) throws IOException {
        if (nome == null || nome.isBlank()) {
            return false;
        }
        this.nome = nome;
        salvarUsuario();
        return true;
    }

    public String getNome() {
        return nome;
    }
}

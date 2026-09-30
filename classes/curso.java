import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Curso implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final FileStorage<Curso> STORAGE =
            new FileStorage<>(Path.of("dados", "cursos.bin"), Curso.class);
    private static final FileStorage<Disciplina> DISCIPLINAS =
            new FileStorage<>(Path.of("dados", "disciplinas.bin"), Disciplina.class);

    private final String nome;
    private final String credito;
    private final List<String> codigosDisciplinas = new ArrayList<>();

    public Curso(String nome, String credito) {
        if (nome == null || nome.isBlank() || credito == null || credito.isBlank()) {
            throw new IllegalArgumentException("Nome e créditos do curso são obrigatórios.");
        }
        this.nome = nome;
        this.credito = credito;
    }

    public void salvar() throws IOException {
        List<Curso> cursos = STORAGE.readAll();
        cursos.removeIf(curso -> curso.nome.equals(nome));
        cursos.add(this);
        STORAGE.writeAll(cursos);
    }

    public boolean adicionarDisciplina(Disciplina disciplina) throws IOException {
        if (disciplina == null) {
            return false;
        }

        List<Curso> cursos = STORAGE.readAll();
        Curso cursoSalvo = cursos.stream()
                .filter(curso -> curso.nome.equals(nome))
                .findFirst().orElse(this);
        if (cursoSalvo.codigosDisciplinas.contains(disciplina.getCodigo())) {
            return false;
        }

        disciplina.associarCurso(nome);
        List<Disciplina> disciplinas = DISCIPLINAS.readAll();
        disciplinas.removeIf(item -> item.getCodigo().equals(disciplina.getCodigo()));
        disciplinas.add(disciplina);
        DISCIPLINAS.writeAll(disciplinas);
        cursoSalvo.codigosDisciplinas.add(disciplina.getCodigo());
        cursos.removeIf(curso -> curso.nome.equals(nome));
        cursos.add(cursoSalvo);
        STORAGE.writeAll(cursos);
        return true;
    }

    public String getNome() {
        return nome;
    }

    public String getCredito() {
        return credito;
    }
}

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

public class Disciplina implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final FileStorage<Disciplina> STORAGE =
            new FileStorage<>(Path.of("dados", "disciplinas.bin"), Disciplina.class);

    private final String codigo;
    private final String nome;
    private final TipoDisciplina tipo;
    private String cursoNome;

    public Disciplina(String codigo, String nome, TipoDisciplina tipo) {
        if (codigo == null || codigo.isBlank() || nome == null || nome.isBlank() || tipo == null) {
            throw new IllegalArgumentException("Código, nome e tipo da disciplina são obrigatórios.");
        }
        this.codigo = codigo;
        this.nome = nome;
        this.tipo = tipo;
    }

    public void salvar() throws IOException {
        List<Disciplina> disciplinas = STORAGE.readAll();
        disciplinas.removeIf(disciplina -> disciplina.codigo.equals(codigo));
        disciplinas.add(this);
        STORAGE.writeAll(disciplinas);
    }

    public boolean matricularAluno(Aluno aluno, Turma turma, String semestre) throws IOException {
        if (turma == null || !turma.getDisciplinaCodigo().equals(codigo)) {
            return false;
        }
        return aluno.matricular(turma, semestre);
    }

    void associarCurso(String cursoNome) {
        this.cursoNome = cursoNome;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public TipoDisciplina getTipo() {
        return tipo;
    }

    public String getCursoNome() {
        return cursoNome;
    }
}

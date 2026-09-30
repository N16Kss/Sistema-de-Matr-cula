import java.io.IOException;

public class Secretaria extends Usuario {
    public Secretaria(String matricula, String senha) {
        super(matricula, senha);
    }

    public Curriculo gerarCurriculo(String semestre) throws IOException {
        Curriculo curriculo = new Curriculo(semestre);
        curriculo.salvar();
        return curriculo;
    }
}

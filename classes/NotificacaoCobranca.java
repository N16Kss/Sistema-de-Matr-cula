import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

public class NotificacaoCobranca implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final FileStorage<NotificacaoCobranca> STORAGE =
            new FileStorage<>(Path.of("dados", "fila-cobranca.bin"), NotificacaoCobranca.class);

    private final String alunoMatricula;
    private final String turmaCodigo;
    private final String semestre;
    private boolean enviada;

    private NotificacaoCobranca(String alunoMatricula, String turmaCodigo, String semestre) {
        this.alunoMatricula = alunoMatricula;
        this.turmaCodigo = turmaCodigo;
        this.semestre = semestre;
    }

    static void enfileirar(String alunoMatricula, String turmaCodigo, String semestre) throws IOException {
        List<NotificacaoCobranca> notificacoes = STORAGE.readAll();
        boolean jaEnfileirada = notificacoes.stream().anyMatch(notificacao ->
                notificacao.alunoMatricula.equals(alunoMatricula)
                        && notificacao.turmaCodigo.equals(turmaCodigo)
                        && notificacao.semestre.equals(semestre));
        if (!jaEnfileirada) {
            notificacoes.add(new NotificacaoCobranca(alunoMatricula, turmaCodigo, semestre));
            STORAGE.writeAll(notificacoes);
        }
    }

    public static List<NotificacaoCobranca> pendentes() throws IOException {
        return STORAGE.readAll().stream().filter(notificacao -> !notificacao.enviada).toList();
    }

    public boolean marcarEnviada() throws IOException {
        List<NotificacaoCobranca> notificacoes = STORAGE.readAll();
        NotificacaoCobranca salva = notificacoes.stream()
                .filter(notificacao -> notificacao.alunoMatricula.equals(alunoMatricula)
                        && notificacao.turmaCodigo.equals(turmaCodigo)
                        && notificacao.semestre.equals(semestre))
                .findFirst().orElse(null);
        if (salva == null || salva.enviada) {
            return false;
        }
        salva.enviada = true;
        enviada = true;
        STORAGE.writeAll(notificacoes);
        return true;
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
}
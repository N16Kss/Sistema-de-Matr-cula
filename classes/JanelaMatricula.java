import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class JanelaMatricula implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final FileStorage<JanelaMatricula> STORAGE =
            new FileStorage<>(Path.of("dados", "periodos-matricula.bin"), JanelaMatricula.class);

    private final String semestre;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;

    public JanelaMatricula(String semestre, LocalDate dataInicio, LocalDate dataFim) {
        if (semestre == null || semestre.isBlank() || dataInicio == null || dataFim == null
                || dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Informe semestre e intervalo de datas válidos.");
        }
        this.semestre = semestre;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    public void salvar() throws IOException {
        List<JanelaMatricula> periodos = STORAGE.readAll();
        periodos.removeIf(periodo -> periodo.semestre.equals(semestre));
        periodos.add(this);
        STORAGE.writeAll(periodos);
    }

    public static boolean estaAberto(String semestre) throws IOException {
        LocalDate hoje = LocalDate.now();
        return STORAGE.readAll().stream()
                .filter(periodo -> periodo.semestre.equals(semestre))
                .anyMatch(periodo -> !hoje.isBefore(periodo.dataInicio) && !hoje.isAfter(periodo.dataFim));
    }
}
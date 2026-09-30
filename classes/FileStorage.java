import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FileStorage<T extends Serializable> {
    private final Path file;
    private final Class<T> type;

    public FileStorage(Path file, Class<T> type) {
        this.file = file;
        this.type = type;
    }

    public List<T> readAll() throws IOException {
        if (Files.notExists(file)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(file))) {
            Object value = input.readObject();
            if (!(value instanceof List<?> values)) {
                throw new IOException("Arquivo de dados inválido: " + file);
            }

            List<T> records = new ArrayList<>(values.size());
            for (Object item : values) {
                if (!type.isInstance(item)) {
                    throw new IOException("Registro incompatível no arquivo: " + file);
                }
                records.add(type.cast(item));
            }
            return records;
        } catch (ClassNotFoundException exception) {
            throw new IOException("Não foi possível ler os dados de " + file, exception);
        }
    }

    public void writeAll(Collection<? extends T> records) throws IOException {
        Path absoluteFile = file.toAbsolutePath();
        Path parent = absoluteFile.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, absoluteFile.getFileName().toString(), ".tmp");

        try {
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(temporaryFile))) {
                output.writeObject(new ArrayList<>(records));
            }

            try {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, absoluteFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }
}
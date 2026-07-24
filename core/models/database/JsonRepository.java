package models.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;


public abstract class JsonRepository<T, ID> {

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final File storageFile;
    private final Function<T, ID> idExtractor;
    private final TypeReference<LinkedHashMap<String, T>> typeReference;
    private final Object lock = new Object();

    protected JsonRepository(String filePath,
                             Function<T, ID> idExtractor,
                             TypeReference<LinkedHashMap<String, T>> typeReference) {
        this.storageFile = new File(filePath);
        this.idExtractor = idExtractor;
        this.typeReference = typeReference;
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            File parent = storageFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            if (!storageFile.exists() || storageFile.length() == 0) {
                mapper.writeValue(storageFile, new LinkedHashMap<String, T>());
            }
        } catch (IOException e) {
            System.err.println("Error creating storage file: " + e.getMessage());
        }
    }

    protected Map<String, T> readAll() {
        synchronized (lock) {
            try {
                return mapper.readValue(storageFile, typeReference);
            } catch (IOException e) {
                System.err.println("Error reading storage file: " + e.getMessage());
                return new LinkedHashMap<>();
            }
        }
    }

    private void writeAll(Map<String, T> data) {
        synchronized (lock) {
            try {
                mapper.writeValue(storageFile, data);
            } catch (IOException e) {
                System.err.println("Error writing storage file: " + e.getMessage());
            }
        }
    }

    public void save(T entity) {
        Map<String, T> data = readAll();
        data.put(String.valueOf(idExtractor.apply(entity)), entity);
        writeAll(data);
    }

    public Optional<T> findById(ID id) {
        return Optional.ofNullable(readAll().get(String.valueOf(id)));
    }

    public List<T> findAll() {
        return new ArrayList<>(readAll().values());
    }



    public Optional<T> findOne(Predicate<T> predicate) {
        return readAll().values().stream().filter(predicate).findFirst();
    }

}
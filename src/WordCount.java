import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class WordCount {

    public static void main(String[] args) {
        String text = "Legends tell of ancient forests where the trees whisper secrets to those who listen. Beneath the canopy, hidden paths reveal forgotten ruins and creatures long thought extinct. But only the brave dare venture, for not all who enter return.";

        // розділяємо текст на частини (тут для прикладу 2)
        List<String> parts = Arrays.asList(
                text.substring(0, text.length() / 2),
                text.substring(text.length() / 2)
        );

        // створюємо ExecutorService з фіксованою кількістю потоків
        ExecutorService executor = Executors.newFixedThreadPool(parts.size());
        ConcurrentHashMap<String, Integer> wordCountMap = new ConcurrentHashMap<>();
        List<Future<ConcurrentHashMap<String, Integer>>> futures;

        // створюємо Callable для кожної частини
        futures = parts.stream()
                .map(part -> executor.submit(() -> {
                    ConcurrentHashMap<String, Integer> localMap = new ConcurrentHashMap<>();
                    String[] words = part.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");
                    for (String word : words) {
                        localMap.merge(word, 1, Integer::sum);
                    }
                    return localMap;
                }))
                .collect(Collectors.toList()); // використовуємо Collectors.toList() для старіших версій Java

        // збираємо результати
        for (Future<ConcurrentHashMap<String, Integer>> future : futures) {
            try {

                ConcurrentHashMap<String, Integer> resultMap = future.get();
                // об'єднаємо результати із загальною мапою
                resultMap.forEach((key, value) -> wordCountMap.merge(key, value, Integer::sum));
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("An error occurred during word counting: " + e.getMessage());
            }
        }

        // завершуємо
        executor.shutdown();

        // результати
        wordCountMap.forEach((word, count) ->
                System.out.println("Word: '" + word + "', Count: " + count)
        );
    }
}
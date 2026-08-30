package com.saas.media_core.service;

import com.saas.media_core.entity.Task;
import com.saas.media_core.enums.TaskStatus;
import com.saas.media_core.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskProcessorService {

    private final TaskRepository taskRepository;
    
    private final LzwProcessorService lzwProcessorService;
    private final DpcmProcessorService dpcmProcessorService;
    private final ShannonFanoProcessorService shannonFanoProcessorService;
    private final DeltaModulationProcessorService deltaModulationProcessorService;
    private final AiNeuralTranslationProcessorService aiNeuralTranslationProcessorService;

    private static final String FILE_NOT_FOUND_MSG = "الملف الأصلي غير موجود على السيرفر!";

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void processPendingTasks() {
        List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);
        if (pendingTasks.isEmpty()) return;

        log.info("⚡ تم العثور على {} مهمة قيد الانتظار. جاري تشغيل محركات المعالجة الشاملة...", pendingTasks.size());

        for (Task task : pendingTasks) {
            try {
                // تحديد هوية صاحب الملف للطباعة في السجلات (Logs)
                String owner = (task.getUser() == null) ? "زائر مجهول (تجربة مجانية)" : task.getUser().getEmail();
                log.info("⚙️ بدء معالجة الملف [{}] الخاص بـ: {}", task.getOriginalFileName(), owner);

                task.setStatus(TaskStatus.PROCESSING);
                taskRepository.save(task);

                String compressedFilePath = executeCompression(task);

                task.setOutputPayload(compressedFilePath);
                task.setStatus(TaskStatus.COMPLETED);
                
                log.info("✅ تمت معالجة المهمة بنجاح! مسار الملف المخرج: {}", compressedFilePath);

            } catch (IOException | RuntimeException e) { 
                task.setStatus(TaskStatus.FAILED);
                log.error("❌ فشلت معالجة المهمة رقم: {}", task.getId(), e);
            }
            taskRepository.save(task);
        }
    }

    private String executeCompression(Task task) throws IOException {
        String sourceFilePath = task.getInputPayload();
        
        Path sourcePath = Paths.get(sourceFilePath);
        if (!Files.exists(sourcePath)) {
            throw new FileNotFoundException(FILE_NOT_FOUND_MSG);
        }

        String targetFilePath;

        switch (task.getAlgorithm()) {
            case HUFFMAN:
                targetFilePath = sourceFilePath + ".gz";
                log.info("🔄 جاري تطبيق خوارزمية HUFFMAN...");
                try (FileInputStream fis = new FileInputStream(sourceFilePath);
                     FileOutputStream fos = new FileOutputStream(targetFilePath);
                     GZIPOutputStream gzipOS = new GZIPOutputStream(fos)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        gzipOS.write(buffer, 0, len);
                    }
                }
                break;

            case LZW:
                targetFilePath = sourceFilePath + ".lzw";
                log.info("🔄 جاري تطبيق خوارزمية LZW...");
                String textData = Files.readString(sourcePath);
                byte[] lzwCompressed = lzwProcessorService.compress(textData);
                Files.write(Paths.get(targetFilePath), lzwCompressed);
                break;

            case DPCM:
                targetFilePath = sourceFilePath + ".dpcm";
                log.info("🔄 جاري تطبيق خوارزمية DPCM...");
                byte[] audioBytes = Files.readAllBytes(sourcePath);
                byte[] dpcmCompressed = dpcmProcessorService.compress(audioBytes);
                Files.write(Paths.get(targetFilePath), dpcmCompressed);
                break;

            case SHANNON_FANO:
                targetFilePath = sourceFilePath + ".sf";
                log.info("🔄 جاري تطبيق خوارزمية SHANNON_FANO...");
                String shannonText = Files.readString(sourcePath);
                byte[] shannonCompressed = shannonFanoProcessorService.compress(shannonText);
                Files.write(Paths.get(targetFilePath), shannonCompressed);
                break;

            case DELTA_MODULATION:
                targetFilePath = sourceFilePath + ".dm";
                log.info("🔄 جاري تطبيق خوارزمية DELTA_MODULATION...");
                byte[] dmBytes = Files.readAllBytes(sourcePath);
                byte[] dmCompressed = deltaModulationProcessorService.compress(dmBytes);
                Files.write(Paths.get(targetFilePath), dmCompressed);
                break;

            case AI_NEURAL_TRANSLATION:
                targetFilePath = sourceFilePath + ".ai.txt";
                log.info("🔄 جاري تطبيق معالج AI_NEURAL_TRANSLATION...");
                String aiInput = Files.readString(sourcePath);
                byte[] aiProcessed = aiNeuralTranslationProcessorService.process(aiInput);
                Files.write(Paths.get(targetFilePath), aiProcessed);
                break;

            default:
                throw new IllegalArgumentException("الخوارزمية المطلوبة غير مدعومة حالياً: " + task.getAlgorithm());
        }
        
        return targetFilePath;
    }
}
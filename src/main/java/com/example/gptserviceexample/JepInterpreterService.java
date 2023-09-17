package com.example.gptserviceexample;

import jep.Jep;
import jep.SharedInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * interpreter is thread local and work only inside the same thread.
 */
@Service
class JepInterpreterService {
    private static Logger LOGGER = LoggerFactory.getLogger(JepInterpreterService.class);
    private static ExecutorService executors  = newExecutors();

    private final ThreadLocal<Jep> jepInterpreter;
    private ThreadLocal<Boolean> isReadyForInference = new ThreadLocal(){
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public JepInterpreterService(@Value("${interpreter.is_shared:true}") boolean isShared, @Value("${gpt.use_gpu:false}") boolean useGPU){
        JepInitializer.initialize(useGPU);
        jepInterpreter =  ThreadLocal.withInitial(() -> JepInitializer.createInterpreter(isShared));
    }
    /**
     * Submit [parseSync] to thread pool and wait for result.
     *
     * @param jepInvocation thread safe invocation of jep interpreter.
     */
    public <T> T invoke(
            Function<Jep, T> jepInvocation
    ) {
        try {
            return executors.submit(() -> {
                        Jep interp = jepInterpreter.get();
                        ensureReadyForInference(interp);
                        return jepInvocation.apply(interp);
                    }
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureReadyForInference(Jep interp) {
        if (!isReadyForInference.get() && JepInitializer.isTrained) {
            JepInitializer.prepareForInference(interp);
            isReadyForInference.set(true);
        }
    }

    private static ExecutorService newExecutors()  {
        SharedInterpreter.setConfig(JepInitializer.config);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int nThreads = Integer.getInteger("python.interpreters.number", availableProcessors);
        LOGGER.info("Creating thread pool: {} logical CPU cores are available. " +
                "Actual pool size will be {} threads.", availableProcessors, nThreads);
        return Executors.newFixedThreadPool( nThreads );
    }

    public void dumpProfilerStats() {
        if(System.getenv("PROFILER_STAT_FILE") != null) {
            LOGGER.info("Dumping profiler statistics ...");

            executors.execute(() -> {
                JepInitializer.dumpProfilerStats(jepInterpreter.get());
            });

            LOGGER.info("Dumping profiler statistics finished.");
        }
    }
}

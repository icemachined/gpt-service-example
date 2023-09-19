package com.example.gptserviceexample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
public class GptController {
    private static Logger LOGGER = LoggerFactory.getLogger(GptController.class);
    @Autowired
    private JepInterpreterService jepService;


    @PostMapping(path="/gpt/poem")
    String generatePoem(@RequestParam String start) throws ExecutionException, InterruptedException {
        LOGGER.info("Start generating a poem");
        try {
            return jepService.invoke(interp -> interp.invoke("generate_text", start).toString()).get();
        } finally {
            LOGGER.info("Finished generating a poem");
        }
    }

    @PostMapping(path="/gpt/train")
    void trainGPT() {
        LOGGER.info("Start gpt training");
        jepService.invoke(interp -> {
            //interp.eval("os.chdir('../..')");
            interp.set("__file__", "train.py");
            interp.runScript("train.py");
            return true;
        });
    }
}

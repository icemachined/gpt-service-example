package com.example.gptserviceexample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GptController {

    @Autowired
    private JepInterpreterService jepService;


    @PostMapping(path="/gpt/poem")
    String generatePoem(@RequestParam String start) {
        return jepService.invoke(interp -> interp.invoke("generate_text", start).toString());
    }

    @PostMapping(path="/gpt/train")
    void trainGPT() {
        jepService.invoke(interp -> {
            //interp.eval("os.chdir('../..')");
            interp.set("__file__", "train.py");
            interp.runScript("train.py");
            JepInitializer.isTrained = true;
            return true;
        });
    }
}

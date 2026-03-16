package com.example.graphrag;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/graphrag")
@RequiredArgsConstructor
public class GraphRagController {

    private  final GraphRagService graphRagService;

    @GetMapping("/ask")
    public String ask(@RequestParam String question){
        return graphRagService.ask(question);
    }

}

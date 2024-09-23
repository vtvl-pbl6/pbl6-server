package com.dut.pbl6_server.controller;

import com.dut.pbl6_server.service.ThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController("PostController")
@RequestMapping("/api/v1/thread")
@RequiredArgsConstructor
public class ThreadController {
    private final ThreadService threadService;

    @PostMapping
    public Object createThread(@RequestBody Map<String, ?> body) {
        return threadService.createThread(body.get("text").toString());
    }

}

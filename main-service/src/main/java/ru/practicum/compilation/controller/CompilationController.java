package ru.practicum.compilation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Controller
@RequestMapping("/compilations")
public class CompilationController {

    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean Pinned,
                                                @RequestParam(defaultValue = "0") Integer From,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return compilationService.getCompilations(Pinned, From, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        return compilationService.getCompilationById(compId);
    }
}

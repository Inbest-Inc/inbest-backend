package com.inbest.backend.controller;

import com.inbest.backend.dto.DonutChartDTO;
import com.inbest.backend.model.response.GenericResponse;
import com.inbest.backend.service.ChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charts/donut")
@RequiredArgsConstructor
public class ChartController {

    private final ChartService chartService;

    @GetMapping("/{portfolioId}")
    public ResponseEntity<GenericResponse> getDonutChartData(@PathVariable Long portfolioId) {
        List<DonutChartDTO> donutChartData = chartService.createDonutChartData(portfolioId);

        GenericResponse response = new GenericResponse(
                "success",
                "Donut chart data fetched successfully",
                donutChartData
        );

        return ResponseEntity.ok(response);
    }
}

package com.smartRahi.SmartRahi.Controller;

import com.smartRahi.SmartRahi.DTO.request.BusRequest;
import com.smartRahi.SmartRahi.DTO.response.BusResponse;
import com.smartRahi.SmartRahi.Services.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;


    @PostMapping/// for create new bus
    public BusResponse createBus(@RequestBody BusRequest request) {
        return busService.createBus(request);
    }

    @GetMapping("/{busId}")
    public BusResponse getBusById(@PathVariable String busId) {
        return busService.getBusById(busId);
    }

    @GetMapping
    public List<BusResponse> getAllBuses() {
        return busService.getAllBuses();
    }

    @PutMapping("/{busId}")
    public BusResponse updateBus(@PathVariable String busId, @RequestBody BusRequest request) {
        return busService.updateBus(busId, request);
    }

    @DeleteMapping("/{busId}")
    public void deleteBus(@PathVariable String busId) {
        busService.deleteBus(busId);
    }
}

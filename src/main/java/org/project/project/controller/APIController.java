package org.project.project.controller;

import org.project.project.model.entity.API;
import org.project.project.service.APIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devportal/apis")
public class APIController {

    @Autowired
    private APIService apiService;

    @GetMapping
    public List<API> getAllApis() {
        return apiService.listarApis();
    }

    @GetMapping("/{id}")
    public ResponseEntity<API> getApiById(@PathVariable Long id) {
        API api = apiService.buscarApiPorId(id);
        return ResponseEntity.ok(api);
    }

    @PostMapping
    public API createApi(@RequestBody API api) {
        return apiService.guardarApi(api);
    }

    @PutMapping("/{id}")
    public ResponseEntity<API> updateApi(@PathVariable Long id, @RequestBody API apiDetails) {
        API updatedApi = apiService.actualizarApi(id, apiDetails);
        return ResponseEntity.ok(updatedApi);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApi(@PathVariable Long id) {
        apiService.eliminarApi(id);
        return ResponseEntity.noContent().build();
    }
}
package com.example.banking.Controllers;


import com.example.banking.Models.Pack;
import com.example.banking.Repositories.PackRepository;
import com.example.banking.Services.dto.PackRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Controller
@RequestMapping("/api/packs")
public class PackController {
    private PackRepository packRepository;

    public PackController(PackRepository packRepository){
        this.packRepository = packRepository;
    }
    @GetMapping("/getPacks")
    public List<Pack> getAllPacks(){
        return packRepository.findAll();
    }

    @PostMapping("/addPack")
    public Pack addPack(@Valid @RequestBody PackRequest packRequest){

        Pack pack = new Pack();

        pack.setName(packRequest.getName());
        pack.setDescription(packRequest.getDescription());
        pack.setInsurance(packRequest.isInsurance());
        pack.setSupportLevel(packRequest.getSupportLevel());
        pack.setMaxTransactionsPerDay(packRequest.getMaxTransactionsPerDay());
        pack.setMonthlyFee(packRequest.getMonthlyFee());
        return packRepository.save(pack);
    }



    @DeleteMapping("/deletePack/{id}")
    public ResponseEntity<?> deletePack(@PathVariable Long id) {
        if (packRepository.existsById(id)) {
            packRepository.deleteById(id);
            return ResponseEntity.ok().body(
                    Map.of(
                            "success", true,
                            "message", "Pack with ID " + id + " deleted successfully"
                    )
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of(
                            "success", false,
                            "message", "Pack with ID " + id + " not found"
                    )
            );
        }
    }
}

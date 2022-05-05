package py.una.pol.rest.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import py.una.pol.rest.model.Demand;
import py.una.pol.rest.model.Options;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SimuladorController {

     public void simular(Options options) throws Exception {
        List<Demand> demands;

     }
}

package org.cmdb4j.server.rest;

import java.util.ArrayList;
import java.util.List;

import org.cmdb4j.core.env.CmdbEnvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.val;

@RestController
@RequestMapping(path = "/api/envs")
@OpenAPIDefinition(
		info = @Info(title = "Envs API")
		)
public class Cmdb4jEnvsResourceController {

	@Autowired
	private CmdbEnvRepository envRepository;
	
	@Operation(description = "list env names")
	@GetMapping(path="/env-names")
	public List<String> getEnvNames() {
		val tmpres = envRepository.listEnvs();
		return new ArrayList<>(tmpres);
	}

}

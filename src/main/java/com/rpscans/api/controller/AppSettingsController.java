package com.rpscans.api.controller;
import com.rpscans.api.entity.AppSetting;
import com.rpscans.api.repository.AppSettingRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@RestController
@RequestMapping("/api/settings")
public class AppSettingsController {
  private final AppSettingRepository repository;
  private final ObjectMapper mapper;
  public AppSettingsController(AppSettingRepository repository,ObjectMapper mapper){this.repository=repository;this.mapper=mapper;}
  @GetMapping
  public Map<String,Object> get(){
    Map<String,Object> result=new LinkedHashMap<>();
    repository.findAll().forEach(row->{
      try {result.put(row.key,mapper.readTree(row.value));}
      catch(Exception e){result.put(row.key,row.value);}
    });
    return result;
  }
  @PutMapping
  @Transactional
  public Map<String,Object> save(@RequestBody Map<String,JsonNode> body){
    if(body.size()>100)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Too many settings");
    for(var entry:body.entrySet()){
      String key=entry.getKey();JsonNode value=entry.getValue();
      if(!key.matches("[a-zA-Z][a-zA-Z0-9]{0,119}")||value==null||!(value.isTextual()||value.isBoolean()||value.isNumber()))
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid setting: "+key);
      String json=value.toString();
      if(json.length()>10000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Setting too long: "+key);
      repository.save(new AppSetting(key,json));
    }
    return get();
  }
}

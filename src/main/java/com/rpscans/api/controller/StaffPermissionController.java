package com.rpscans.api.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpscans.api.entity.StaffPermission;
import com.rpscans.api.repository.StaffPermissionRepository;
import com.rpscans.api.repository.StaffRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/staff/{staffId}/permissions")
public class StaffPermissionController {
  private final StaffPermissionRepository permissions;
  private final StaffRepository staff;
  private final ObjectMapper mapper;
  public StaffPermissionController(StaffPermissionRepository permissions,StaffRepository staff,ObjectMapper mapper){
    this.permissions=permissions;this.staff=staff;this.mapper=mapper;
  }
  public record PermissionDto(boolean loginAccess,Map<String,Map<String,Boolean>> permissions){}
  private void ensureStaff(Long id){
    if(!staff.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Staff not found");
  }
  @GetMapping
  public PermissionDto get(@PathVariable Long staffId){
    ensureStaff(staffId);
    StaffPermission row=permissions.findById(staffId).orElse(null);
    if(row==null)return new PermissionDto(false,Map.of());
    try {
      Map<String,Map<String,Boolean>> data=mapper.readValue(row.permissionsJson,new TypeReference<>(){});
      return new PermissionDto(row.loginAccess,data);
    } catch(JsonProcessingException e){
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Stored permissions are invalid");
    }
  }
  @PutMapping
  public PermissionDto save(@PathVariable Long staffId,@RequestBody PermissionDto dto){
    ensureStaff(staffId);
    if(dto.permissions()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Permissions required");
    StaffPermission row=permissions.findById(staffId).orElseGet(()->{
      StaffPermission value=new StaffPermission();value.staffId=staffId;return value;
    });
    row.loginAccess=dto.loginAccess();
    try{row.permissionsJson=mapper.writeValueAsString(dto.permissions());}
    catch(JsonProcessingException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid permissions");}
    permissions.save(row);
    return get(staffId);
  }
}

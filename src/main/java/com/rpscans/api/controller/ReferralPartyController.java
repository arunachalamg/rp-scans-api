package com.rpscans.api.controller;
import com.rpscans.api.entity.ReferralParty; import com.rpscans.api.repository.ReferralPartyRepository; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/referrals") public class ReferralPartyController { private final ReferralPartyRepository repo; public ReferralPartyController(ReferralPartyRepository r){repo=r;}
 @GetMapping public List<ReferralParty> all(){return repo.findAll();} @PostMapping public ReferralParty add(@RequestBody ReferralParty x){x.id=null;return repo.save(x);} @PutMapping("/{id}") public ReferralParty update(@PathVariable Long id,@RequestBody ReferralParty x){x.id=id;return repo.save(x);} }

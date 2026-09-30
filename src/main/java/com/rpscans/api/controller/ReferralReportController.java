package com.rpscans.api.controller;
import com.rpscans.api.repository.ReferralReportRepository; import org.springframework.web.bind.annotation.*; import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/reports/referrals") public class ReferralReportController { private final ReferralReportRepository repo; public ReferralReportController(ReferralReportRepository r){repo=r;}
 @GetMapping("/summary") public List<Map<String,Object>> summary(@RequestParam LocalDate from,@RequestParam LocalDate to){return repo.summary(from,to).stream().map(x->{Map<String,Object> m=new LinkedHashMap<>();m.put("referralId",x[0]);m.put("name",x[1]);m.put("type",x[2]);m.put("totalPatients",x[3]);m.put("totalAmount",x[4]);m.put("totalReferralAmount",x[5]);return m;}).toList();}
}

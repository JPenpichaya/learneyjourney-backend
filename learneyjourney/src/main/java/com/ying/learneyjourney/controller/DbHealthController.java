package com.ying.learneyjourney.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
public class DbHealthController {
    private final DataSource ds;
    DbHealthController(DataSource ds) { this.ds = ds; }

    @GetMapping("/dbcheck")
    String check() throws Exception {
        try (var c = ds.getConnection();
             var st = c.createStatement();
             var rs = st.executeQuery("select now()")) {
            rs.next();
            return "DB OK: " + rs.getString(1);
        }
    }
}

package com.example.algorithm.controller;

import com.example.algorithm.service.SettingService;
import com.example.algorithm.service.SwitchConfiguratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/avionics")
@RequiredArgsConstructor
public class Controller {
    private final SettingService settingService;
    private final SwitchConfiguratorService switchConfiguratorService;

    @GetMapping // switch, flow, routing 정보 세팅, feasible_Bag_mtu 구함
    public String setting() throws IOException {
        File file = new File("C:\\Users\\hhj48\\Desktop\\message.txt");
        settingService.set_switch_flow(file);
        settingService.find_feasible_BAG_MTU();
        return "setting & find feasible";
    }

    @GetMapping("/{name}") // name에 switch 이름 입력하면 optimal solution 저장
    public String SwitchConfig(@PathVariable String name) {
        switchConfiguratorService.find_minimum_bandwidth_configuration(name); // 스위치 별로 configurator
        return "optimal solution";
    }
}

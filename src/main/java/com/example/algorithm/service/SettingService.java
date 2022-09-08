package com.example.algorithm.service;
import com.example.algorithm.model.*;
import com.example.algorithm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingService {

    private final SwitchRepository switchRepository;
    private final FlowRepository flowRepository;
    private final SwitchVLRepository switchVLRepository;
    private final VLRepository vlRepository;
    private final Feasible_BAG_MTURepository feasibleBag_mtuRepository;

    @Transactional
    public boolean set_switch_flow(File file) throws IOException{
        FileReader fileReader = new FileReader(file);
        BufferedReader bufReader = new BufferedReader(fileReader);
        int switch_len = Integer.parseInt(bufReader.readLine());

        // 스위치 정보 저장
        for(int i=0;i<switch_len;i++){
            String str[] = bufReader.readLine().split(" ");
            String switch_name = str[0]; // switch 이름
            int bandwidth = Integer.parseInt(str[1]);
            Switch swt = new Switch(switch_name,bandwidth);
            switchRepository.save(swt);
        }

        // VL 정보 및 해당하는 Flow 정보 저장
        int vl_len = Integer.parseInt(bufReader.readLine()); // 총 vl 개수
        for(int i=0;i<vl_len;i++){
            String str[] = bufReader.readLine().split(" ");
            String vl_name = str[0]; // vl 이름
            VL vl = new VL(vl_name);
            vlRepository.save(vl);
            int flow_len = Integer.parseInt(str[1]); // flow 개수
            for(int j=0;j<flow_len;j++){ // VL내 flow 세팅
                str = bufReader.readLine().split(" ");
                int l = Integer.parseInt(str[0]);
                int p = Integer.parseInt(str[1]);
                Flow flow = new Flow(l,p,vl);
                flowRepository.save(flow);
            }

            // 해당 VL의 routing 정보(switch들) 저장
            str = bufReader.readLine().split(" ");
            for(int j=0;j<str.length;j++){ // switch 개수만큼 돌기
                String swit = str[j];
                String switch_name = swit;
                Switch switc = switchRepository.findSwitchByName(switch_name);
                VL vl_routing = vlRepository.findVLByName(vl_name);
                Switch_VL switch_vl = new Switch_VL(switc,vl_routing);
                switchVLRepository.save(switch_vl);
            }
        }
        return true;
    }

    @Transactional
    public boolean find_feasible_BAG_MTU(){ // 각 링크 별로 실현 가능한 BAG_MTU쌍 구하기

        for(int i=1;i<=vlRepository.count();i++){ // VLi
          HashSet<Integer> N = new HashSet<>();
          //  가능한 MTU 집합 구하기
          for(Flow x : vlRepository.findById((long)i).get().getFlows()) { // F(i,j)
              double frag = Math.ceil((double)x.getL()/(Math.ceil((double)x.getL()/x.getP())));
              while(frag>=1.0) {
                  int m = (int)Math.ceil((double)x.getL()/frag);
                  N.add(m);
                  frag--;
              }
          }

          // 최대 페이로드 구하기 (MTU 크기가 최대 재생로드 크기보다 크면 필요한 Utilization은 변경 X)
          int max_payload = Integer.MIN_VALUE;
          for(Flow x:vlRepository.findById((long)i).get().getFlows()) {
              if(x.getL()>max_payload)
                  max_payload = x.getL();
          }

          for(int k=0;k<=7;k++) { // BAG 지정
              Iterator iter = N.iterator();
              int least_m = Integer.MAX_VALUE;
              while(iter.hasNext()) { // MTU 집합들 중
                  int m = (int)iter.next();
                  double sum=0;
                  for(Flow x : vlRepository.findById((long)i).get().getFlows()) {
                      sum += Math.ceil((double)x.getL()/m)/x.getP();
                  }

                  if(sum<=1/Math.pow(2, k)) {
                      if(least_m > m) {
                          least_m = m;
                      }
                  }
              }
              if(max_payload<least_m)
                  break;

              Optional<VL> vl = vlRepository.findById((long)i);
              Feasible_BAG_MTU bag_mtu = new Feasible_BAG_MTU((int)Math.pow(2,k),least_m,vl.get()); // vl과 mtu_bag 관계 성립
              feasibleBag_mtuRepository.save(bag_mtu);
          }
      }
      return true;
    }





}

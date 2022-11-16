package com.example.algorithm.service;
import com.example.algorithm.dto.BAG_MTU_VL;
import com.example.algorithm.dto.Node;
import com.example.algorithm.model.*;
import com.example.algorithm.repository.Optimal_BAG_MTURepository;
import com.example.algorithm.repository.SwitchRepository;
import com.example.algorithm.repository.SwitchVLRepository;
import com.example.algorithm.repository.VLRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwitchConfiguratorService {
    private final SwitchRepository switchRepository;
    private final SwitchVLRepository switchVLRepository;
    private final Optimal_BAG_MTURepository optimal_bag_mtuRepository;
    private final VLRepository vlRepository;

    // 각 switch에 해당하는 VL당 최적의 BAG_MTU쌍 구하기
    @Transactional
    public boolean find_minimum_bandwidth_configuration(){
        PriorityQueue<Node> pqueue = new PriorityQueue<>();
        List<Switch> switc = switchRepository.findAll(); // 모든 스위치 정보

        // 스위치 이름 --> 여러 가상링크들 정보
        HashMap<String,List<VL>> total_switch_vls = new HashMap<>();
        for(int i=0;i<switc.size();i++){
            Switch swit = switc.get(i);
            List<Switch_VL> switch_vls = switchVLRepository.findSwitch_VLSBySwitc(swit);
            List<VL> vls = switch_vls.stream().map(Switch_VL::getVl).collect(Collectors.toList());
            total_switch_vls.put(swit.getName(),vls);
        }

        List<VL> total_vls = vlRepository.findAll();

        int N = total_vls.size();
        HashMap<String,double[]> Bmap = new HashMap<>();
        HashMap<String,double[]> Jmap = new HashMap<>();

        for(int i=0;i<switc.size();i++){
            double B[] = new double[N+1];
            double J[] = new double[N+1];
            Bmap.put(switc.get(i).getName(),B);
            Jmap.put(switc.get(i).getName(),J);
        }

        HashMap<String, Double> total_B0= new HashMap<>();
        HashMap<String, Double> total_J0= new HashMap<>();

        // bound 값 구하기 (최소 대역폭, 최소 Jitter)
       // B 최소값
        for(int i=0;i<switc.size();i++) {
            Double B0 = 0.0;
            Double J0 = 0.0;

            List<VL> vls = total_switch_vls.get(switc.get(i).getName());
            for (int j = 1; j <= vls.size(); j++) {
                double B_min = Double.MAX_VALUE;
                for (Feasible_BAG_MTU s : vls.get(j - 1).getFeasible_pair()) { // vl id 기준 (vl id=1 --> B[1])
                    double B_result = (s.getMTU() + 67) / s.getBAG();
                    if (B_min > B_result) {
                        B_min = B_result;
                    }
                }

                double b[] = Bmap.get(switc.get(i).getName());
                b[(vls.get(j-1).getId()).intValue()] = B_min;// 각 가상 링크당 최소의 Bound

                // J 최소값
                double jitter[] = Jmap.get(switc.get(i).getName());
                jitter[(vls.get(j-1).getId()).intValue()] = vls.get(j-1).getFeasible_pair().get(0).getMTU()+67; // 제일 상위 순번이 가장 작은 MTU

                B0+=B_min;
                J0+=jitter[(vls.get(j-1).getId()).intValue()];
            }

            // 스위치에서 흐르는 가상링크들의  Bandwidth, Jitter의 최소값 합
            total_B0.put(switc.get(i).getName(),B0);
            total_J0.put(switc.get(i).getName(),J0);
        }
        System.out.println("siwtch크기: "+switc.size());
        for(int switch_i=0;switch_i<switc.size();switch_i++){
            Switch current_sw = switc.get(switch_i);
            String switch_name = current_sw.getName();
            System.out.println(switch_name);
            pqueue = new PriorityQueue<>();
            pqueue.offer(new Node(0,total_B0.get(switch_name),total_J0.get(switch_name),null));
            int BANDWIDTH = switchRepository.findSwitchByName(switch_name).getBandwidth(); // 스위치의 대역폭 값

            boolean solution_found = false;
            while(!pqueue.isEmpty() && solution_found==false) {
                Node node = pqueue.poll(); // 가장 적은 bound 가진 node 꺼냄 (priority queue) --> node level 파악 용도
                for (BAG_MTU_VL y : node.getS()) {
                    System.out.println("selected: " + node.getBound() + "," + node.getJitter() + "  BAG:" + y.getBAG() + " " + "MTU:" + y.getMTU() + " (" + "vl:" + y.getVl().getId() + ")");
                }
                System.out.println();
                if (node.getLevel() == current_sw.getSiwtch_vls().size()) { // VL의 개수만큼 다 돌면 return
                    for (BAG_MTU_VL bag_mtu_vl : node.getS()) {
                        Optimal_BAG_MTU optimal_bag_mtu = new Optimal_BAG_MTU(switch_name, bag_mtu_vl.getBAG(), bag_mtu_vl.getMTU(), bag_mtu_vl.getVl());
                        optimal_bag_mtuRepository.save(optimal_bag_mtu);
                    }
                    solution_found = true;
                }
                if (solution_found == false) {
                    int i = node.getLevel() + 1; //vl1, vl2
                    List<Feasible_BAG_MTU> feasible_B_M = total_switch_vls.get(switch_name).get(i - 1).getFeasible_pair();
                    for (Feasible_BAG_MTU bag_mtu : feasible_B_M) {
                        double Bcur = node.getBound() - Bmap.get(switch_name)[i] + (bag_mtu.getMTU() + 67) / bag_mtu.getBAG(); // bandwidth (특정 vl 제외 최소 대역폭 총 합에서 가능한 BAG&MTU 쌍에 따른 대역폭 구해서 모두 검사)
                        double Jcur = node.getJitter() - Jmap.get(switch_name)[i] + bag_mtu.getMTU() + 67; // jitter
                        if (Bcur <= BANDWIDTH / 8000 && Jcur <= 460 * BANDWIDTH) {
                            ArrayList<BAG_MTU_VL> tmp = new ArrayList<>(node.getS());
                            tmp.add(new BAG_MTU_VL(bag_mtu.getBAG(), bag_mtu.getMTU(), total_switch_vls.get(switch_name).get(i - 1))); // 해당 vl 에서 조건을 만족하는 bag&mtu 쌍 저장
                            pqueue.offer(new Node(i, Bcur, Jcur, tmp));
                        }
                    }
                    for (Node x : pqueue) {
//                if(x.getS().size()==N){
                        for (BAG_MTU_VL y : x.getS()) {
                            System.out.println("BAG:" + y.getBAG() + " " + "MTU:" + y.getMTU() + " (" + "vl:" + y.getVl().getId() + ")");
                        }
                        System.out.println(x.getBound() + "," + x.getJitter() + "," + x.getLevel());
                        System.out.println();
//                }
                    }
                }
            }
        }
        return false;
    }

    public boolean find_minimum_bandwidth_configuration_global(){
        PriorityQueue<Node> pqueue = new PriorityQueue<>();
        List<Switch> switc = switchRepository.findAll(); // 모든 스위치 정보

        // 스위치 이름 --> 여러 가상링크들 정보
        HashMap<String,List<VL>> total_switch_vls = new HashMap<>();
        for(int i=0;i<switc.size();i++){
            Switch swit = switc.get(i);
            List<Switch_VL> switch_vls = switchVLRepository.findSwitch_VLSBySwitc(swit);
            List<VL> vls = switch_vls.stream().map(Switch_VL::getVl).collect(Collectors.toList());
            total_switch_vls.put(swit.getName(),vls);
        }

        List<VL> total_vls = vlRepository.findAll();

        int N = total_vls.size();
        HashMap<String,double[]> Bmap = new HashMap<>();
        HashMap<String,double[]> Jmap = new HashMap<>();

        for(int i=0;i<switc.size();i++){
            double B[] = new double[N+1];
            double J[] = new double[N+1];
            Bmap.put(switc.get(i).getName(),B);
            Jmap.put(switc.get(i).getName(),J);
        }

        HashMap<String, Double> total_B0= new HashMap<>();
        HashMap<String, Double> total_J0= new HashMap<>();


        // bound 값 구하기 (최소 대역폭, 최소 Jitter)
        // B 최소값
        for(int i=0;i<switc.size();i++) {
            Double B0 = 0.0;
            Double J0 = 0.0;

            List<VL> vls = total_switch_vls.get(switc.get(i).getName());
            for (int j = 1; j <= vls.size(); j++) {
                double B_min = Double.MAX_VALUE;
                for (Feasible_BAG_MTU s : vls.get(j - 1).getFeasible_pair()) { // vl id 기준 (vl id=1 --> B[1])
                    double B_result = (s.getMTU() + 67) / s.getBAG();
                    if (B_min > B_result) {
                        B_min = B_result;
                    }
                }

                double b[] = Bmap.get(switc.get(i).getName());
                b[(vls.get(j-1).getId()).intValue()] = B_min;// 각 가상 링크당 최소의 Bound

                // J 최소값
                double jitter[] = Jmap.get(switc.get(i).getName());
                jitter[(vls.get(j-1).getId()).intValue()] = vls.get(j-1).getFeasible_pair().get(0).getMTU()+67; // 제일 상위 순번이 가장 작은 MTU

                B0+=B_min;
                J0+=jitter[(vls.get(j-1).getId()).intValue()];
            }

            // 스위치에서 흐르는 가상링크들의  Bandwidth, Jitter의 최소값 합
            total_B0.put(switc.get(i).getName(),B0);
            total_J0.put(switc.get(i).getName(),J0);
        }



        for(int i=0;i<total_vls.size();i++){
            VL vl = total_vls.get(i);
        }



        System.out.println("siwtch크기: "+switc.size());
        for(int switch_i=0;switch_i<switc.size();switch_i++){
            Switch current_sw = switc.get(switch_i);
            String switch_name = current_sw.getName();
            System.out.println(switch_name);
            pqueue = new PriorityQueue<>();
            pqueue.offer(new Node(0,total_B0.get(switch_name),total_J0.get(switch_name),null));
            int BANDWIDTH = switchRepository.findSwitchByName(switch_name).getBandwidth(); // 스위치의 대역폭 값

            boolean solution_found = false;
            while(!pqueue.isEmpty() && solution_found==false) {
                Node node = pqueue.poll(); // 가장 적은 bound 가진 node 꺼냄 (priority queue) --> node level 파악 용도
                for (BAG_MTU_VL y : node.getS()) {
                    System.out.println("selected: " + node.getBound() + "," + node.getJitter() + "  BAG:" + y.getBAG() + " " + "MTU:" + y.getMTU() + " (" + "vl:" + y.getVl().getId() + ")");
                }
                System.out.println();
                if (node.getLevel() == current_sw.getSiwtch_vls().size()) { // VL의 개수만큼 다 돌면 return
                    for (BAG_MTU_VL bag_mtu_vl : node.getS()) {
                        Optimal_BAG_MTU optimal_bag_mtu = new Optimal_BAG_MTU(switch_name, bag_mtu_vl.getBAG(), bag_mtu_vl.getMTU(), bag_mtu_vl.getVl());
                        optimal_bag_mtuRepository.save(optimal_bag_mtu);
                    }
                    solution_found = true;
                }
                if (solution_found == false) {
                    int i = node.getLevel() + 1; //vl1, vl2
                    List<Feasible_BAG_MTU> feasible_B_M = total_switch_vls.get(switch_name).get(i - 1).getFeasible_pair();
                    for (Feasible_BAG_MTU bag_mtu : feasible_B_M) {
                        double Bcur = node.getBound() - Bmap.get(switch_name)[i] + (bag_mtu.getMTU() + 67) / bag_mtu.getBAG(); // bandwidth (특정 vl 제외 최소 대역폭 총 합에서 가능한 BAG&MTU 쌍에 따른 대역폭 구해서 모두 검사)
                        double Jcur = node.getJitter() - Jmap.get(switch_name)[i] + bag_mtu.getMTU() + 67; // jitter
                        if (Bcur <= BANDWIDTH / 8000 && Jcur <= 460 * BANDWIDTH) {
                            ArrayList<BAG_MTU_VL> tmp = new ArrayList<>(node.getS());
                            tmp.add(new BAG_MTU_VL(bag_mtu.getBAG(), bag_mtu.getMTU(), total_switch_vls.get(switch_name).get(i - 1))); // 해당 vl 에서 조건을 만족하는 bag&mtu 쌍 저장
                            pqueue.offer(new Node(i, Bcur, Jcur, tmp));
                        }
                    }
                    for (Node x : pqueue) {
//                if(x.getS().size()==N){
                        for (BAG_MTU_VL y : x.getS()) {
                            System.out.println("BAG:" + y.getBAG() + " " + "MTU:" + y.getMTU() + " (" + "vl:" + y.getVl().getId() + ")");
                        }
                        System.out.println(x.getBound() + "," + x.getJitter() + "," + x.getLevel());
                        System.out.println();
//                }
                    }
                }
            }
        }
        return false;



    }


}

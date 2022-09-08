package com.example.algorithm.service;
import com.example.algorithm.dto.BAG_MTU_VL;
import com.example.algorithm.dto.Node;
import com.example.algorithm.model.*;
import com.example.algorithm.repository.Optimal_BAG_MTURepository;
import com.example.algorithm.repository.SwitchRepository;
import com.example.algorithm.repository.SwitchVLRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.ArrayList;
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

    // 각 switch에 해당하는 VL당 최적의 BAG_MTU쌍 구하기
    @Transactional
    public boolean find_minimum_bandwidth_configuration(String name){
        PriorityQueue<Node> pqueue = new PriorityQueue<>();
        Switch switc = switchRepository.findSwitchByName(name);
        List<Switch_VL> switch_vls = switchVLRepository.findSwitch_VLSBySwitc(switc);
        List<VL> vls = switch_vls.stream().map(Switch_VL::getVl).collect(Collectors.toList());

        int N = switch_vls.size();
        double B[] = new double[N+1];
        double J[] = new double[N+1];

        // bound 값 구하기 (최소 대역폭, 최소 Jitter)
       // B 최소값
        for(int i=1;i<=N;i++){
            double B_min = Double.MAX_VALUE;
            for(Feasible_BAG_MTU s : vls.get(i-1).getFeasible_pair()){ // vl id 기준 (vl id=1 --> B[1])
                double B_result = (s.getMTU()+67)/s.getBAG();
                if(B_min>B_result){
                    B_min = B_result;
                }
            }
            B[i] = B_min; // 각 가상 링크당 최소의 Bound
        }

        // J 최소값
        for(int i=1;i<=N;i++){
            J[i] = vls.get(i-1).getFeasible_pair().get(0).getMTU()+67; // 제일 상위 순번이 가장 작은 MTU
        }

        Double B0 = 0.0;
        Double J0 = 0.0;

        for(int i=1;i<=N;i++){ // 스위치에서 흐르는 가상링크들의  Bandwidth, Jitter의 최소값 합
            B0 += B[i]; // 116.0
            J0 += J[i]; // 207.0
        }

        System.out.println(B0+","+J0);
        pqueue.offer(new Node(0,B0,J0,null));
        int BANDWIDTH = switchRepository.findSwitchByName(name).getBandwidth(); // 스위치의 대역폭 값

        while(!pqueue.isEmpty()){
            Node node = pqueue.poll(); // 가장 적은 bound 가진 node 꺼냄 (priority queue) --> node level 파악 용도
            for(BAG_MTU_VL y : node.getS()){
                System.out.println("selected: "+node.getBound()+","+node.getJitter()+"  BAG:"+ y.getBAG()+" "+"MTU:"+ y.getMTU()+" ("+"vl:"+y.getVl().getId()+")");
            }
            System.out.println();
            if(node.getLevel()==N){ // VL의 개수만큼 다 돌면 return
                for(BAG_MTU_VL bag_mtu_vl: node.getS()){
                    Optimal_BAG_MTU optimal_bag_mtu = new Optimal_BAG_MTU(name,bag_mtu_vl.getBAG(),bag_mtu_vl.getMTU(),bag_mtu_vl.getVl());
                    optimal_bag_mtuRepository.save(optimal_bag_mtu);
                }
                return true;
            }

            int i = node.getLevel()+1; //vl1, vl2
            List<Feasible_BAG_MTU> feasible_B_M = vls.get(i-1).getFeasible_pair();
            for(Feasible_BAG_MTU bag_mtu : feasible_B_M){
                double Bcur = node.getBound()-B[i]+(bag_mtu.getMTU()+67)/bag_mtu.getBAG(); // bandwidth (특정 vl 제외 최소 대역폭 총 합에서 가능한 BAG&MTU 쌍에 따른 대역폭 구해서 모두 검사)
                double Jcur = node.getJitter()-J[i]+bag_mtu.getMTU()+67; // jitter
                if(Bcur<=BANDWIDTH/8000 && Jcur<=460*BANDWIDTH){
                    ArrayList<BAG_MTU_VL> tmp = new ArrayList<>(node.getS());
                    tmp.add(new BAG_MTU_VL(bag_mtu.getBAG(),bag_mtu.getMTU(),vls.get(i-1))); // 해당 vl 에서 조건을 만족하는 bag&mtu 쌍 저장
                    pqueue.offer(new Node(i,Bcur,Jcur,tmp));
                }
            }
            for(Node x : pqueue){
//                if(x.getS().size()==N){
                    for(BAG_MTU_VL y : x.getS()){
                        System.out.println("BAG:"+ y.getBAG()+" "+"MTU:"+ y.getMTU()+" ("+"vl:"+y.getVl().getId()+")");
                    }
                System.out.println(x.getBound()+","+x.getJitter()+","+x.getLevel());
                    System.out.println();
//                }
            }
        }
        return false;
    }

}

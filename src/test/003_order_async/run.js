import http from "k6/http";
import { sleep } from "k6";
import { Trend } from "k6/metrics";
import variables from "./variables.js";

// waiting time을 기록할 Trend metric 생성
let waitingTrend = new Trend("http_req_waiting_time");
export const options = {
  vus: variables.virtualUsers,
  duration: variables.duration,
};

// 서버 포트: 5232 (application.yml 확인)
// WSL에서 Windows 호스트 접근을 위해 Windows 호스트 IP 사용
const url = "http://172.16.24.145:5232/internal/v1/orders/payment/success";
export default function () {
  const res = http.post(url);
  console.log(`Status: ${res.status} | Waiting: ${res.timings.waiting} ms`);
  sleep(1); // 각 VU 1초 쉬고 반복
}
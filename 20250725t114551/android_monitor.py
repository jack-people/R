#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Android模拟器监控系统
让AI能够实时观察和分析Android应用的运行效果
"""

import subprocess
import threading
import time
import cv2
import numpy as np
import pyaudio
import wave
import json
import os
from datetime import datetime
from typing import Dict, List, Optional, Tuple
import base64
from io import BytesIO
from PIL import Image

class AndroidEmulatorMonitor:
    """Android模拟器监控核心类"""
    
    def __init__(self, device_id: str = None):
        self.device_id = device_id or self._get_default_device()
        self.is_monitoring = False
        self.screen_data = []
        self.audio_data = []
        self.log_data = []
        
        # 配置参数
        self.screen_capture_fps = 10  # 屏幕捕获帧率
        self.audio_sample_rate = 44100
        self.audio_channels = 2
        self.audio_chunk_size = 1024
        
        # 初始化组件
        self.screen_recorder = ScreenRecorder(self)
        self.audio_recorder = AudioRecorder(self)
        self.log_monitor = LogMonitor(self)
        self.ai_analyzer = AIAnalyzer(self)
        
        print(f"✅ Android监控系统初始化完成，设备ID: {self.device_id}")
    
    def _get_default_device(self) -> str:
        """获取默认Android设备ID"""
        try:
            result = subprocess.run(['adb', 'devices'], 
                                  capture_output=True, text=True, check=True)
            lines = result.stdout.strip().split('\n')[1:]  # 跳过标题行
            devices = [line.split('\t')[0] for line in lines if '\tdevice' in line]
            
            if devices:
                print(f"🔍 发现Android设备: {devices}")
                return devices[0]
            else:
                print("⚠️ 未发现Android设备，请确保模拟器已启动")
                return "emulator-5554"  # 默认模拟器ID
        except Exception as e:
            print(f"❌ 获取设备列表失败: {e}")
            return "emulator-5554"
    
    def start_monitoring(self):
        """开始监控"""
        if self.is_monitoring:
            print("⚠️ 监控已在运行中")
            return
        
        self.is_monitoring = True
        print("🚀 开始监控Android模拟器...")
        
        # 启动各个监控线程
        threading.Thread(target=self.screen_recorder.start, daemon=True).start()
        threading.Thread(target=self.audio_recorder.start, daemon=True).start()
        threading.Thread(target=self.log_monitor.start, daemon=True).start()
        threading.Thread(target=self.ai_analyzer.start, daemon=True).start()
        
        print("✅ 所有监控模块已启动")
    
    def stop_monitoring(self):
        """停止监控"""
        self.is_monitoring = False
        print("🛑 停止监控...")
    
    def get_current_status(self) -> Dict:
        """获取当前监控状态"""
        return {
            "is_monitoring": self.is_monitoring,
            "device_id": self.device_id,
            "screen_frames": len(self.screen_data),
            "audio_samples": len(self.audio_data),
            "log_entries": len(self.log_data),
            "timestamp": datetime.now().isoformat()
        }


class ScreenRecorder:
    """屏幕录制模块"""
    
    def __init__(self, monitor: AndroidEmulatorMonitor):
        self.monitor = monitor
        self.last_screenshot = None
        self.ui_changes = []
    
    def start(self):
        """开始屏幕录制"""
        print("📱 屏幕录制模块启动")
        
        while self.monitor.is_monitoring:
            try:
                # 使用ADB截屏
                screenshot = self._capture_screen()
                if screenshot is not None:
                    # 检测UI变化
                    changes = self._detect_ui_changes(screenshot)
                    if changes:
                        self.ui_changes.extend(changes)
                    
                    # 保存截图数据
                    self.monitor.screen_data.append({
                        "timestamp": datetime.now().isoformat(),
                        "image": self._image_to_base64(screenshot),
                        "changes": changes
                    })
                    
                    # 限制数据量，只保留最近的100帧
                    if len(self.monitor.screen_data) > 100:
                        self.monitor.screen_data.pop(0)
                
                time.sleep(1.0 / self.monitor.screen_capture_fps)
                
            except Exception as e:
                print(f"❌ 屏幕捕获错误: {e}")
                time.sleep(1)
    
    def _capture_screen(self) -> Optional[np.ndarray]:
        """捕获屏幕截图"""
        try:
            # 使用ADB截屏命令
            cmd = ['adb', '-s', self.monitor.device_id, 'exec-out', 'screencap', '-p']
            result = subprocess.run(cmd, capture_output=True, check=True)
            
            # 将字节数据转换为图像
            image_array = np.frombuffer(result.stdout, dtype=np.uint8)
            image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)
            
            return image
            
        except Exception as e:
            print(f"❌ 截屏失败: {e}")
            return None
    
    def _detect_ui_changes(self, current_image: np.ndarray) -> List[Dict]:
        """检测UI变化"""
        changes = []
        
        if self.last_screenshot is not None:
            try:
                # 计算图像差异
                diff = cv2.absdiff(current_image, self.last_screenshot)
                gray_diff = cv2.cvtColor(diff, cv2.COLOR_BGR2GRAY)
                
                # 查找变化区域
                _, thresh = cv2.threshold(gray_diff, 30, 255, cv2.THRESH_BINARY)
                contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
                
                for contour in contours:
                    area = cv2.contourArea(contour)
                    if area > 1000:  # 过滤小变化
                        x, y, w, h = cv2.boundingRect(contour)
                        changes.append({
                            "type": "ui_change",
                            "area": int(area),
                            "position": {"x": int(x), "y": int(y), "width": int(w), "height": int(h)},
                            "timestamp": datetime.now().isoformat()
                        })
                        
            except Exception as e:
                print(f"❌ UI变化检测错误: {e}")
        
        self.last_screenshot = current_image.copy()
        return changes
    
    def _image_to_base64(self, image: np.ndarray) -> str:
        """将图像转换为base64字符串"""
        try:
            _, buffer = cv2.imencode('.jpg', image, [cv2.IMWRITE_JPEG_QUALITY, 80])
            image_base64 = base64.b64encode(buffer).decode('utf-8')
            return image_base64
        except Exception as e:
            print(f"❌ 图像编码错误: {e}")
            return ""


class AudioRecorder:
    """音频录制模块"""
    
    def __init__(self, monitor: AndroidEmulatorMonitor):
        self.monitor = monitor
        self.audio_interface = None
        self.audio_stream = None
    
    def start(self):
        """开始音频录制"""
        print("🔊 音频录制模块启动")
        
        try:
            self.audio_interface = pyaudio.PyAudio()
            
            # 配置音频流
            self.audio_stream = self.audio_interface.open(
                format=pyaudio.paInt16,
                channels=self.monitor.audio_channels,
                rate=self.monitor.audio_sample_rate,
                input=True,
                frames_per_buffer=self.monitor.audio_chunk_size
            )
            
            while self.monitor.is_monitoring:
                try:
                    # 录制音频数据
                    audio_data = self.audio_stream.read(self.monitor.audio_chunk_size)
                    
                    # 分析音频特征
                    audio_features = self._analyze_audio(audio_data)
                    
                    # 保存音频数据
                    self.monitor.audio_data.append({
                        "timestamp": datetime.now().isoformat(),
                        "data": base64.b64encode(audio_data).decode('utf-8'),
                        "features": audio_features
                    })
                    
                    # 限制数据量
                    if len(self.monitor.audio_data) > 1000:
                        self.monitor.audio_data.pop(0)
                        
                except Exception as e:
                    print(f"❌ 音频录制错误: {e}")
                    time.sleep(0.1)
                    
        except Exception as e:
            print(f"❌ 音频初始化失败: {e}")
        finally:
            self._cleanup()
    
    def _analyze_audio(self, audio_data: bytes) -> Dict:
        """分析音频特征"""
        try:
            # 将字节数据转换为numpy数组
            audio_array = np.frombuffer(audio_data, dtype=np.int16)
            
            # 计算音频特征
            volume = np.sqrt(np.mean(audio_array**2))
            max_amplitude = np.max(np.abs(audio_array))
            
            return {
                "volume": float(volume),
                "max_amplitude": int(max_amplitude),
                "has_sound": volume > 100  # 阈值可调整
            }
            
        except Exception as e:
            print(f"❌ 音频分析错误: {e}")
            return {"volume": 0, "max_amplitude": 0, "has_sound": False}
    
    def _cleanup(self):
        """清理音频资源"""
        if self.audio_stream:
            self.audio_stream.stop_stream()
            self.audio_stream.close()
        if self.audio_interface:
            self.audio_interface.terminate()


class LogMonitor:
    """日志监控模块"""
    
    def __init__(self, monitor: AndroidEmulatorMonitor):
        self.monitor = monitor
        self.log_process = None
    
    def start(self):
        """开始日志监控"""
        print("📋 日志监控模块启动")
        
        try:
            # 启动logcat进程
            cmd = ['adb', '-s', self.monitor.device_id, 'logcat', '-v', 'time']
            self.log_process = subprocess.Popen(
                cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, 
                text=True, bufsize=1, universal_newlines=True
            )
            
            while self.monitor.is_monitoring and self.log_process:
                try:
                    line = self.log_process.stdout.readline()
                    if line:
                        log_entry = self._parse_log_line(line.strip())
                        if log_entry:
                            self.monitor.log_data.append(log_entry)
                            
                            # 限制日志数量
                            if len(self.monitor.log_data) > 1000:
                                self.monitor.log_data.pop(0)
                    else:
                        time.sleep(0.1)
                        
                except Exception as e:
                    print(f"❌ 日志读取错误: {e}")
                    time.sleep(1)
                    
        except Exception as e:
            print(f"❌ 日志监控启动失败: {e}")
        finally:
            self._cleanup()
    
    def _parse_log_line(self, line: str) -> Optional[Dict]:
        """解析日志行"""
        try:
            # 简单的日志解析，可以根据需要扩展
            if len(line) > 20:
                return {
                    "timestamp": datetime.now().isoformat(),
                    "content": line,
                    "level": self._extract_log_level(line)
                }
        except Exception as e:
            print(f"❌ 日志解析错误: {e}")
        return None
    
    def _extract_log_level(self, line: str) -> str:
        """提取日志级别"""
        for level in ['E/', 'W/', 'I/', 'D/', 'V/']:
            if level in line:
                return level[0]
        return 'U'  # Unknown
    
    def _cleanup(self):
        """清理日志进程"""
        if self.log_process:
            self.log_process.terminate()


class AIAnalyzer:
    """AI分析模块"""
    
    def __init__(self, monitor: AndroidEmulatorMonitor):
        self.monitor = monitor
        self.analysis_results = []
    
    def start(self):
        """开始AI分析"""
        print("🤖 AI分析模块启动")
        
        while self.monitor.is_monitoring:
            try:
                # 分析最新的数据
                analysis = self._analyze_current_state()
                if analysis:
                    self.analysis_results.append(analysis)
                    print(f"🔍 AI分析: {analysis['summary']}")
                    
                    # 限制分析结果数量
                    if len(self.analysis_results) > 100:
                        self.analysis_results.pop(0)
                
                time.sleep(2)  # 每2秒分析一次
                
            except Exception as e:
                print(f"❌ AI分析错误: {e}")
                time.sleep(5)
    
    def _analyze_current_state(self) -> Optional[Dict]:
        """分析当前状态"""
        try:
            # 获取最新数据
            recent_screen = self.monitor.screen_data[-5:] if self.monitor.screen_data else []
            recent_audio = self.monitor.audio_data[-10:] if self.monitor.audio_data else []
            recent_logs = self.monitor.log_data[-20:] if self.monitor.log_data else []
            
            if not any([recent_screen, recent_audio, recent_logs]):
                return None
            
            # 生成分析报告
            analysis = {
                "timestamp": datetime.now().isoformat(),
                "summary": self._generate_summary(recent_screen, recent_audio, recent_logs),
                "screen_analysis": self._analyze_screen_data(recent_screen),
                "audio_analysis": self._analyze_audio_data(recent_audio),
                "log_analysis": self._analyze_log_data(recent_logs)
            }
            
            return analysis
            
        except Exception as e:
            print(f"❌ 状态分析错误: {e}")
            return None
    
    def _generate_summary(self, screen_data: List, audio_data: List, log_data: List) -> str:
        """生成分析摘要"""
        summary_parts = []
        
        # 屏幕分析
        if screen_data:
            ui_changes = sum(len(frame.get('changes', [])) for frame in screen_data)
            if ui_changes > 0:
                summary_parts.append(f"检测到{ui_changes}个UI变化")
        
        # 音频分析
        if audio_data:
            has_sound = any(sample.get('features', {}).get('has_sound', False) for sample in audio_data)
            if has_sound:
                summary_parts.append("检测到音频活动")
        
        # 日志分析
        if log_data:
            error_count = sum(1 for log in log_data if log.get('level') == 'E')
            if error_count > 0:
                summary_parts.append(f"发现{error_count}个错误日志")
        
        return "；".join(summary_parts) if summary_parts else "系统运行正常"
    
    def _analyze_screen_data(self, screen_data: List) -> Dict:
        """分析屏幕数据"""
        if not screen_data:
            return {"status": "无屏幕数据"}
        
        total_changes = sum(len(frame.get('changes', [])) for frame in screen_data)
        return {
            "frames_analyzed": len(screen_data),
            "total_ui_changes": total_changes,
            "activity_level": "高" if total_changes > 10 else "中" if total_changes > 3 else "低"
        }
    
    def _analyze_audio_data(self, audio_data: List) -> Dict:
        """分析音频数据"""
        if not audio_data:
            return {"status": "无音频数据"}
        
        volumes = [sample.get('features', {}).get('volume', 0) for sample in audio_data]
        avg_volume = sum(volumes) / len(volumes) if volumes else 0
        
        return {
            "samples_analyzed": len(audio_data),
            "average_volume": round(avg_volume, 2),
            "sound_detected": any(sample.get('features', {}).get('has_sound', False) for sample in audio_data)
        }
    
    def _analyze_log_data(self, log_data: List) -> Dict:
        """分析日志数据"""
        if not log_data:
            return {"status": "无日志数据"}
        
        level_counts = {}
        for log in log_data:
            level = log.get('level', 'U')
            level_counts[level] = level_counts.get(level, 0) + 1
        
        return {
            "logs_analyzed": len(log_data),
            "level_distribution": level_counts,
            "has_errors": level_counts.get('E', 0) > 0
        }


def main():
    """主函数"""
    print("🚀 启动Android模拟器监控系统")
    
    # 创建监控实例
    monitor = AndroidEmulatorMonitor()
    
    try:
        # 开始监控
        monitor.start_monitoring()
        
        # 主循环
        while True:
            time.sleep(5)
            status = monitor.get_current_status()
            print(f"📊 监控状态: 屏幕帧数={status['screen_frames']}, "
                  f"音频样本={status['audio_samples']}, 日志条数={status['log_entries']}")
            
    except KeyboardInterrupt:
        print("\n🛑 用户中断，正在停止监控...")
        monitor.stop_monitoring()
    except Exception as e:
        print(f"❌ 系统错误: {e}")
    finally:
        print("👋 监控系统已退出")


if __name__ == "__main__":
    main()
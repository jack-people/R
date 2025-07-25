#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Android监控系统演示脚本
快速测试AI观察功能
"""

import time
import json
from datetime import datetime
from android_monitor import AndroidEmulatorMonitor

def demo_ai_observation():
    """演示AI观察功能"""
    print("🎬 Android AI监控系统演示")
    print("=" * 50)
    
    # 创建监控实例
    monitor = AndroidEmulatorMonitor()
    
    print("🚀 启动监控...")
    monitor.start_monitoring()
    
    try:
        # 运行演示
        for i in range(30):  # 运行30秒
            time.sleep(1)
            
            # 显示实时状态
            status = monitor.get_current_status()
            print(f"\r⏱️  运行时间: {i+1}s | "
                  f"屏幕帧: {status['screen_frames']} | "
                  f"音频样本: {status['audio_samples']} | "
                  f"日志条数: {status['log_entries']}", end="")
            
            # 每5秒显示AI分析
            if (i + 1) % 5 == 0 and hasattr(monitor, 'ai_analyzer'):
                print("\n")
                if monitor.ai_analyzer.analysis_results:
                    latest = monitor.ai_analyzer.analysis_results[-1]
                    print(f"🤖 AI观察: {latest['summary']}")
                    
                    # 显示详细分析
                    if latest.get('screen_analysis'):
                        screen = latest['screen_analysis']
                        print(f"   📱 屏幕活动: {screen.get('activity_level', '未知')}")
                    
                    if latest.get('audio_analysis'):
                        audio = latest['audio_analysis']
                        print(f"   🔊 音频状态: {'有声音' if audio.get('sound_detected') else '静音'}")
                    
                    if latest.get('log_analysis'):
                        logs = latest['log_analysis']
                        print(f"   📋 系统状态: {'有错误' if logs.get('has_errors') else '正常'}")
                print("-" * 50)
        
        print("\n\n📊 演示完成！最终统计:")
        final_status = monitor.get_current_status()
        print(f"• 总屏幕帧数: {final_status['screen_frames']}")
        print(f"• 总音频样本: {final_status['audio_samples']}")
        print(f"• 总日志条数: {final_status['log_entries']}")
        
        # 显示AI分析历史
        if hasattr(monitor, 'ai_analyzer') and monitor.ai_analyzer.analysis_results:
            print(f"• AI分析次数: {len(monitor.ai_analyzer.analysis_results)}")
            print("\n🧠 AI观察历史:")
            for i, analysis in enumerate(monitor.ai_analyzer.analysis_results[-5:], 1):
                print(f"   {i}. {analysis['summary']}")
        
    except KeyboardInterrupt:
        print("\n🛑 演示被用户中断")
    finally:
        monitor.stop_monitoring()
        print("👋 演示结束")

if __name__ == "__main__":
    demo_ai_observation()
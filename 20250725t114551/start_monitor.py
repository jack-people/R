#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Android监控系统启动脚本
"""

import sys
import os
import subprocess
import time
import threading
from android_monitor import AndroidEmulatorMonitor
from web_interface import app, socketio

def check_dependencies():
    """检查系统依赖"""
    print("🔍 检查系统依赖...")
    
    # 检查ADB
    try:
        result = subprocess.run(['adb', 'version'], capture_output=True, text=True)
        if result.returncode == 0:
            print("✅ ADB已安装")
        else:
            print("❌ ADB未正确安装")
            return False
    except FileNotFoundError:
        print("❌ 未找到ADB，请安装Android SDK")
        return False
    
    # 检查Python包
    required_packages = [
        'flask', 'flask_socketio', 'cv2', 'numpy', 
        'pyaudio', 'PIL'
    ]
    
    missing_packages = []
    for package in required_packages:
        try:
            if package == 'cv2':
                import cv2
            elif package == 'PIL':
                from PIL import Image
            else:
                __import__(package)
            print(f"✅ {package} 已安装")
        except ImportError:
            print(f"❌ {package} 未安装")
            missing_packages.append(package)
    
    if missing_packages:
        print(f"\n请安装缺失的包: pip install {' '.join(missing_packages)}")
        return False
    
    return True

def check_android_devices():
    """检查Android设备"""
    print("📱 检查Android设备...")
    
    try:
        result = subprocess.run(['adb', 'devices'], capture_output=True, text=True)
        lines = result.stdout.strip().split('\n')[1:]
        devices = [line.split('\t')[0] for line in lines if '\tdevice' in line]
        
        if devices:
            print(f"✅ 发现设备: {devices}")
            return True
        else:
            print("⚠️ 未发现Android设备")
            print("请确保:")
            print("1. Android模拟器已启动")
            print("2. USB调试已开启（真机）")
            print("3. 设备已通过ADB连接")
            return False
            
    except Exception as e:
        print(f"❌ 设备检查失败: {e}")
        return False

def start_web_interface():
    """启动Web界面"""
    print("🌐 启动Web监控界面...")
    print("📱 请在浏览器中访问: http://localhost:5000")
    
    try:
        socketio.run(app, host='0.0.0.0', port=5000, debug=False)
    except Exception as e:
        print(f"❌ Web界面启动失败: {e}")

def main():
    """主函数"""
    print("=" * 60)
    print("🤖 Android模拟器AI监控系统")
    print("让AI实时观察和分析您的Android应用运行效果")
    print("=" * 60)
    
    # 检查依赖
    if not check_dependencies():
        print("\n❌ 依赖检查失败，请解决上述问题后重试")
        return
    
    # 检查设备
    if not check_android_devices():
        print("\n⚠️ 设备检查警告，但系统仍可启动")
        print("您可以稍后在Web界面中手动指定设备ID")
    
    print("\n🚀 启动监控系统...")
    
    try:
        # 启动Web界面
        start_web_interface()
        
    except KeyboardInterrupt:
        print("\n🛑 用户中断，正在退出...")
    except Exception as e:
        print(f"\n❌ 系统错误: {e}")
    finally:
        print("👋 监控系统已退出")

if __name__ == "__main__":
    main()
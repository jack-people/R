#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
简化版Android监控系统Web界面
不使用SocketIO，使用轮询方式
"""

from flask import Flask, render_template, jsonify, request
import json
import threading
import time
from cloud_monitor import CloudAndroidMonitor

app = Flask(__name__)

# 全局监控实例
monitor = None

@app.route('/')
def index():
    """主页面"""
    return render_template('simple_index.html')

@app.route('/api/status')
def get_status():
    """获取监控状态API"""
    if monitor:
        return jsonify(monitor.get_current_status())
    return jsonify({
        "error": "监控未启动", 
        "mode": "cloud",
        "is_monitoring": False,
        "screen_frames": 0,
        "audio_samples": 0,
        "log_entries": 0
    })

@app.route('/api/start', methods=['POST'])
def start_monitoring():
    """启动监控API"""
    global monitor
    
    try:
        if monitor and monitor.is_monitoring:
            return jsonify({"error": "监控已在运行"})
        
        monitor = CloudAndroidMonitor()
        monitor.start_monitoring()
        
        return jsonify({
            "success": True, 
            "message": "云端监控已启动", 
            "mode": "cloud"
        })
        
    except Exception as e:
        return jsonify({"error": f"启动失败: {str(e)}"})

@app.route('/api/stop', methods=['POST'])
def stop_monitoring():
    """停止监控API"""
    global monitor
    
    if monitor:
        monitor.stop_monitoring()
        return jsonify({"success": True, "message": "监控已停止"})
    
    return jsonify({"error": "监控未运行"})

@app.route('/api/analysis')
def get_analysis():
    """获取AI分析结果API"""
    if monitor and hasattr(monitor, 'ai_analyzer'):
        return jsonify({
            "analysis_results": monitor.ai_analyzer.analysis_results[-5:],
            "total_count": len(monitor.ai_analyzer.analysis_results),
            "mode": "cloud"
        })
    return jsonify({
        "analysis_results": [],
        "total_count": 0,
        "mode": "cloud"
    })

@app.route('/api/screen')
def get_screen_data():
    """获取屏幕数据API"""
    if monitor and monitor.screen_data:
        return jsonify({
            "latest_screen": monitor.screen_data[-1],
            "recent_changes": [frame.get('changes', []) for frame in monitor.screen_data[-3:]],
            "mode": "cloud"
        })
    return jsonify({
        "latest_screen": None,
        "recent_changes": [],
        "mode": "cloud"
    })

@app.route('/api/logs')
def get_logs():
    """获取日志数据API"""
    if monitor and monitor.log_data:
        return jsonify({
            "recent_logs": monitor.log_data[-10:],
            "total_count": len(monitor.log_data),
            "mode": "cloud"
        })
    return jsonify({
        "recent_logs": [],
        "total_count": 0,
        "mode": "cloud"
    })

if __name__ == '__main__':
    print("🌐 启动简化版云端Android监控界面...")
    print("📱 访问地址将在部署完成后显示")
    app.run(host='0.0.0.0', port=5000, debug=False)
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
云端Android监控系统Web界面
"""

from flask import Flask, render_template, jsonify, request
from flask_socketio import SocketIO, emit
import json
import threading
import time
from cloud_monitor import CloudAndroidMonitor

app = Flask(__name__)
app.config['SECRET_KEY'] = 'cloud_android_monitor'
socketio = SocketIO(app, cors_allowed_origins="*")

# 全局监控实例
monitor = None
monitor_thread = None

@app.route('/')
def index():
    """主页面"""
    return render_template('cloud_index.html')

@app.route('/api/status')
def get_status():
    """获取监控状态API"""
    if monitor:
        return jsonify(monitor.get_current_status())
    return jsonify({"error": "监控未启动", "mode": "cloud"})

@app.route('/api/start', methods=['POST'])
def start_monitoring():
    """启动监控API"""
    global monitor, monitor_thread
    
    try:
        if monitor and monitor.is_monitoring:
            return jsonify({"error": "监控已在运行"})
        
        monitor = CloudAndroidMonitor()
        monitor.start_monitoring()
        
        # 启动数据推送线程
        monitor_thread = threading.Thread(target=push_data_to_clients, daemon=True)
        monitor_thread.start()
        
        return jsonify({"success": True, "message": "云端监控已启动", "mode": "cloud"})
        
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
            "analysis_results": monitor.ai_analyzer.analysis_results[-10:],
            "total_count": len(monitor.ai_analyzer.analysis_results),
            "mode": "cloud"
        })
    return jsonify({"error": "分析数据不可用", "mode": "cloud"})

@socketio.on('connect')
def handle_connect():
    """WebSocket连接处理"""
    print('🔗 客户端已连接到云端监控')
    emit('status', {'message': '已连接到云端监控系统', 'mode': 'cloud'})

@socketio.on('disconnect')
def handle_disconnect():
    """WebSocket断开处理"""
    print('🔌 客户端已断开连接')

def push_data_to_clients():
    """向客户端推送实时数据"""
    while monitor and monitor.is_monitoring:
        try:
            # 推送监控状态
            status = monitor.get_current_status()
            socketio.emit('monitor_status', status)
            
            # 推送最新的屏幕变化
            if monitor.screen_data:
                latest_screen = monitor.screen_data[-1]
                socketio.emit('screen_update', {
                    'timestamp': latest_screen['timestamp'],
                    'changes': latest_screen.get('changes', []),
                    'demo_frame': latest_screen.get('demo_frame', 0),
                    'mode': 'cloud'
                })
            
            # 推送AI分析结果
            if hasattr(monitor, 'ai_analyzer') and monitor.ai_analyzer.analysis_results:
                latest_analysis = monitor.ai_analyzer.analysis_results[-1]
                socketio.emit('ai_analysis', latest_analysis)
            
            time.sleep(2)  # 每2秒推送一次
            
        except Exception as e:
            print(f"❌ 数据推送错误: {e}")
            time.sleep(5)

if __name__ == '__main__':
    print("🌐 启动云端Android监控界面...")
    print("📱 访问地址将在部署完成后显示")
    socketio.run(app, host='0.0.0.0', port=5000, debug=False)
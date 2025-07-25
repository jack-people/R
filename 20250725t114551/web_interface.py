#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Android监控系统Web界面
提供实时的可视化监控面板
"""

from flask import Flask, render_template, jsonify, request
from flask_socketio import SocketIO, emit
import json
import threading
import time
from android_monitor import AndroidEmulatorMonitor

app = Flask(__name__)
app.config['SECRET_KEY'] = 'android_monitor_secret'
socketio = SocketIO(app, cors_allowed_origins="*")

# 全局监控实例
monitor = None
monitor_thread = None

@app.route('/')
def index():
    """主页面"""
    return render_template('index.html')

@app.route('/api/status')
def get_status():
    """获取监控状态API"""
    if monitor:
        return jsonify(monitor.get_current_status())
    return jsonify({"error": "监控未启动"})

@app.route('/api/start', methods=['POST'])
def start_monitoring():
    """启动监控API"""
    global monitor, monitor_thread
    
    try:
        device_id = request.json.get('device_id') if request.json else None
        
        if monitor and monitor.is_monitoring:
            return jsonify({"error": "监控已在运行"})
        
        monitor = AndroidEmulatorMonitor(device_id)
        monitor.start_monitoring()
        
        # 启动数据推送线程
        monitor_thread = threading.Thread(target=push_data_to_clients, daemon=True)
        monitor_thread.start()
        
        return jsonify({"success": True, "message": "监控已启动"})
        
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
            "analysis_results": monitor.ai_analyzer.analysis_results[-10:],  # 最近10条分析
            "total_count": len(monitor.ai_analyzer.analysis_results)
        })
    return jsonify({"error": "分析数据不可用"})

@socketio.on('connect')
def handle_connect():
    """WebSocket连接处理"""
    print('🔗 客户端已连接')
    emit('status', {'message': '已连接到监控系统'})

@socketio.on('disconnect')
def handle_disconnect():
    """WebSocket断开处理"""
    print('🔌 客户端已断开')

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
                    'image': latest_screen['image'][:1000] + '...' if len(latest_screen['image']) > 1000 else latest_screen['image']  # 截断图像数据
                })
            
            # 推送音频状态
            if monitor.audio_data:
                latest_audio = monitor.audio_data[-1]
                socketio.emit('audio_update', {
                    'timestamp': latest_audio['timestamp'],
                    'features': latest_audio['features']
                })
            
            # 推送AI分析结果
            if hasattr(monitor, 'ai_analyzer') and monitor.ai_analyzer.analysis_results:
                latest_analysis = monitor.ai_analyzer.analysis_results[-1]
                socketio.emit('ai_analysis', latest_analysis)
            
            time.sleep(1)  # 每秒推送一次
            
        except Exception as e:
            print(f"❌ 数据推送错误: {e}")
            time.sleep(5)

if __name__ == '__main__':
    print("🌐 启动Web监控界面...")
    print("📱 请在浏览器中访问: http://localhost:5000")
    socketio.run(app, host='0.0.0.0', port=5000, debug=True)
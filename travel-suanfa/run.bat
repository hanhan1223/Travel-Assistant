@echo off
chcp 65001 >nul
echo 正在启动 Python 算法推荐服务...
echo.

REM 检查 Python 是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo 错误：未检测到 Python，请先安装 Python 3.9+
    pause
    exit /b 1
)

REM 检查依赖是否安装
if not exist "venv" (
    echo 正在创建虚拟环境...
    python -m venv venv
    if errorlevel 1 (
        echo 创建虚拟环境失败
        pause
        exit /b 1
    )
)

REM 激活虚拟环境
call venv\Scripts\activate.bat

REM 安装依赖
echo 正在安装依赖包...
pip install -r requirements.txt

echo.
echo ========================================
echo 服务启动中...
echo 访问地址：http://localhost:8000
echo API 文档：http://localhost:8000/docs
echo 按 Ctrl+C 停止服务
echo ========================================
echo.

REM 启动服务
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload

pause
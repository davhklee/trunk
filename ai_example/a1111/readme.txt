automatic1111 is a stable diffusion webui

===requirements===
python3.10.16 CLI/VENV/DEV
opencv-python
controlnet
deforum
ReActor/InsightFace==0.7.3
LORA
Realistic Vision 5.1
gradio==3.16.2 (???)
Roop (obsoleted)
[openvino/cuda stable diffusion webui]
https://github.com/openvinotoolkit/stable-diffusion-webui.git
    hash e5a634da06c62d72dbdc764b16c65ef3408aa588
https://github.com/openvinotoolkit/stable-diffusion-webui/wiki/Installation-on-Intel-Silicon
[safetensors checkpoint]
https://huggingface.co/stable-diffusion-v1-5/stable-diffusion-v1-5/blob/main/v1-5-pruned-emaonly.safetensors
[deadsnakes ppa]
sudo add-apt-repository ppa:deadsnakes/ppa
[controlnet models]
https://github.com/Mikubill/sd-webui-controlnet
https://huggingface.co/lllyasviel/ControlNet/tree/main/models
https://civitai.com/models/9251/controlnet-pre-trained-models
https://civitai.com/models/9868/controlnet-pre-trained-difference-models
[troubleshoot guide]
https://github.com/AUTOMATIC1111/stable-diffusion-webui/wiki/Troubleshooting
[ReActor]
https://www.youtube.com/watch?v=JFq-JcgsAS8
https://www.nextdiffusion.ai/tutorials/how-to-face-swap-in-stable-diffusion-with-reactor-extension
https://github.com/Gourieff/sd-webui-reactor
    hash 2c6a6e63527dc0e8ba9e620a324cb817306cea4b

===installation===
setup deadsnakes PPA to install Python3.10 CLI and VENV
create Python3.10 venv and activate
clone stable diffusion webui from GitHub above
configure webui with export
>export PYTORCH_TRACING_MODE=TORCHFX
>export COMMANDLINE_ARGS="--skip-torch-cuda-test --precision full --no-half --disable-model-loading-ram-optimization"
launch  ./webui.sh
install controlnet extension for webui
install controlnet pretrained models from huggingface or civitai
- copy .pth/.safetensors to install_dir/extensions/sd-webui-controlnet/models
- download and replace checkpoint if corrupted install_dir/models/Stable_Diffusion
configure controlnet preprocessor and select corresponding models
install deforum extension for webui


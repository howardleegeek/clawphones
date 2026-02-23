import logging
from typing import Any, Dict, List, Optional


logger = logging.getLogger("android_framework")


class DeviceCapabilities:
    def __init__(self, task_id: str) -> None:
        self.task_id = task_id
        self.has_npu = False
        self.has_gpu = False
        self.has_dsp = False
        self.memory_mb = 0
        self.cpu_cores = 0

    def detect(self) -> Dict[str, Any]:
        self.has_npu = True
        self.has_gpu = True
        self.has_dsp = True
        self.memory_mb = 8192
        self.cpu_cores = 8
        return self.to_dict()

    def to_dict(self) -> Dict[str, Any]:
        return {
            "has_npu": self.has_npu,
            "has_gpu": self.has_gpu,
            "has_dsp": self.has_dsp,
            "memory_mb": self.memory_mb,
            "cpu_cores": self.cpu_cores,
        }


class AndroidFramework:
    def __init__(self, task_id: str, config: Optional[Dict[str, Any]] = None) -> None:
        self.task_id = task_id
        self.config = config or {}
        self.platform = "Android"
        self.logger = logger
        self._capabilities = DeviceCapabilities(task_id)
        self._loaded_models: Dict[str, bool] = {}
        self._inference_count = 0

    def get_device_capabilities(self) -> Dict[str, Any]:
        self.logger.debug("Detecting device capabilities", extra={
            "task_id": self.task_id,
            "platform": self.platform,
        })
        return self._capabilities.detect()

    def _validate_inference_inputs(self, model: str, input_data: Dict[str, Any]) -> None:
        # Centralized validation to keep run_inference compact
        if not model:
            self.logger.error("Inference failed: missing model", extra={
                "task_id": self.task_id,
                "platform": self.platform,
            })
            raise ValueError("Model name is required")
        if not isinstance(input_data, dict):
            self.logger.error("Inference failed: input_data must be a dict", extra={
                "task_id": self.task_id,
                "platform": self.platform,
            })
            raise ValueError("input_data must be a dict")

    def _validate_batch_inputs(self, model: str, inputs: List[Dict[str, Any]]) -> None:
        # Centralized validation for batch inferences
        if not model:
            self.logger.error("Batch inference failed: missing model", extra={
                "task_id": self.task_id,
                "platform": self.platform,
            })
            raise ValueError("Model name is required")
        if not isinstance(inputs, list):
            self.logger.error("Batch inference failed: inputs must be a list", extra={
                "task_id": self.task_id,
                "platform": self.platform,
            })
            raise ValueError("inputs must be a list")

    def load_model(self, model: str) -> Dict[str, Any]:
        if not model:
            self.logger.error("Model load failed: missing model name", extra={
                "task_id": self.task_id,
                "platform": self.platform,
            })
            raise ValueError("Model name is required")
        
        if model in self._loaded_models:
            self.logger.warning("Model already loaded", extra={
                "task_id": self.task_id,
                "model": model,
            })
            return {"status": "already_loaded", "model": model}

        self.logger.info("Loading model", extra={
            "task_id": self.task_id,
            "model": model,
        })
        self._loaded_models[model] = True
        return {"status": "loaded", "model": model}

    def unload_model(self, model: str) -> Dict[str, Any]:
        if not model:
            self.logger.error("Model unload failed: missing model name", extra={
                "task_id": self.task_id,
                "platform": self.platform,
            })
            raise ValueError("Model name is required")

        if model not in self._loaded_models:
            self.logger.warning("Model not loaded", extra={
                "task_id": self.task_id,
                "model": model,
            })
            return {"status": "not_loaded", "model": model}

        self.logger.info("Unloading model", extra={
            "task_id": self.task_id,
            "model": model,
        })
        del self._loaded_models[model]
        return {"status": "unloaded", "model": model}

    def get_loaded_models(self) -> List[str]:
        return list(self._loaded_models.keys())

    
    def initialize(self) -> Dict[str, Any]:
        self.logger.info("Initializing Android AI framework", extra={
            "task_id": self.task_id,
            "platform": self.platform,
        })
        self._capabilities = DeviceCapabilities(self.task_id)
        self.get_device_capabilities()
        return {"status": "initialized", "platform": self.platform}

    def detect_hardware_acceleration(self) -> Dict[str, Any]:
        caps = self._capabilities.detect()
        accelerators = []
        if caps["has_npu"]:
            accelerators.append("npu")
        if caps["has_gpu"]:
            accelerators.append("gpu")
        if caps["has_dsp"]:
            accelerators.append("dsp")
        
        self.logger.debug("Hardware acceleration detection", extra={
            "task_id": self.task_id,
            "accelerators": accelerators,
        })
        return {"accelerators": accelerators, "available": len(accelerators) > 0}

    def run_inference(self, model: str, input_data: Dict[str, Any]) -> Dict[str, Any]:
        self._inference_count += 1
        self._validate_inference_inputs(model, input_data)

        self.logger.debug("Running inference", extra={
            "task_id": self.task_id,
            "platform": self.platform,
            "model": model,
        })

        if model == "binary":
            predictions = [0.1, 0.9]
        else:
            predictions = [0.3, 0.7]
        output = {"model": model, "predictions": predictions, "input_seen": input_data}
        return {"status": "ok", "output": output}

    def run_batch_inference(self, model: str, inputs: List[Dict[str, Any]]) -> Dict[str, Any]:
        self._inference_count += len(inputs)
        self._validate_batch_inputs(model, inputs)

        self.logger.info("Running batch inference", extra={
            "task_id": self.task_id,
            "model": model,
            "batch_size": len(inputs),
        })

        results = []
        for idx, inp in enumerate(inputs):
            if model == "binary":
                predictions = [0.1, 0.9]
            else:
                predictions = [0.3, 0.7]
            results.append({
                "index": idx,
                "predictions": predictions,
                "input_seen": inp,
            })

        return {"status": "ok", "model": model, "batch_size": len(inputs), "results": results}

    def get_inference_count(self) -> int:
        return self._inference_count

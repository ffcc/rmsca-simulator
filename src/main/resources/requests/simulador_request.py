import requests
import json
import math

def calcular_crosstalk():
    return (2 * math.pow(0.0035, 2) * 0.080) / (4000000 * 0.000045)

url = 'http://localhost:8080/api/v1/simular'

# Elige el valor de XT que deseas (en dB)
xt_db = -25  # Cambia este valor según tu preferencia (-10, -25, o -40)

# Convierte dB a valor lineal
xt = 10**(xt_db/10)

# Definir las variaciones para los parámetros
shortestAlg_values = ["k1", "k3", "k5"]
sortingDemands_values = ["ASC", "DESC", "RANDOM"]

# Iterar sobre cada combinación de shortestAlg y sortingDemands
for alg in shortestAlg_values:
    for sorting in sortingDemands_values:
        payload = {
            "capacity": 320,
            "cores": 7,
            "crosstalkPerUnitLenght": calcular_crosstalk(),
            "demandsQuantity": 1000,
            "fsWidth": 12.5,
            "maxCrosstalk": xt,
            "shortestAlg": alg,
            "sortingDemands": sorting,
            "topology": "usnet.json"
        }

        for i in range(20):
            response = requests.post(url, data=json.dumps(payload), headers={"Content-Type": "application/json"})
            print(f"Request {alg} - {sorting} - {xt_db} dB ({i+1}): {response.status_code}, {response.text}")

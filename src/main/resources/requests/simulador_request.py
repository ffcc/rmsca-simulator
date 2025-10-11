import requests
import json
import math
import time

# URL del endpoint del simulador
url = 'http://localhost:8080/api/v1/simular'

# Parámetros fijos
crosstalk_h_low_insulation = (2 * math.pow(0.0035, 2) * 0.080) / (4000000 * 0.000045) # 1x10^-8
crosstalk_h_high_insulation = (2 * math.pow(0.0004, 2) * 0.05) / (4000000 * 0.00004) # 1x10^-10
current_crosstalk_h = crosstalk_h_high_insulation
current_crosstalk_h_name = "low_insulation" if current_crosstalk_h == crosstalk_h_low_insulation else "high_insulation"

# Variaciones de parámetros
topologies = ["jpn-net.json", "nsfnet.json", "usnet.json"]
dbs = [-10, -25, -40]
ksps = ["k1", "k3", "k5"]
sort_strategies = ["ASC", "DESC", "RANDOM"]
number_of_simulations = 20  # Número de simulaciones por combinación de parámetros
fs_max_config = {
    "-10": 70,
    "-25": 100,
    "-40": 100
}

# Itera por cada variación de parámetros e inicia las simulaciones
total_simulations = len(topologies) * len(dbs) * len(ksps) * len(sort_strategies) * number_of_simulations
total_simulations_error = 0
start_time = time.perf_counter()
for topology in topologies:
    for xt_db in dbs:
        xt = 10**(xt_db/10)
        for alg in ksps:
            for sorting in sort_strategies:
                payload = {
                    "capacity": 320,
                    "cores": 7,
                    "crosstalkPerUnitLenght": current_crosstalk_h,
                    "crosstalkPerUnitLengthName": current_crosstalk_h_name,
                    "demandsQuantity": 1000,
                    "fsWidth": 12.5,
                    "maxCrosstalk": xt,
                    "maxCrosstalkDb": xt_db,
                    "shortestAlg": alg,
                    "sortingDemands": sorting,
                    "topology": topology,
                    #"fsMaxInit": fs_max_config[str(xt_db)]
                }
                print("topology:", topology, "xt_db:", xt_db, "alg:", alg, "sorting:", sorting, "tiempo transcurrido (min):", round((time.perf_counter() - start_time)/60, 2))
                fs_max_results = []
                number_of_active_demands = []
                number_of_blocked_demands = []
                for i in range(number_of_simulations):
                    response = requests.post(url, data=json.dumps(payload), headers={"Content-Type": "application/json"})
                    if response.status_code != 200:
                        print(f'Error en la simulación con topología {topology}, xt_db {xt_db}, alg {alg}, sorting {sorting}. Código de estado: {response.status_code}')
                        total_simulations_error += 1
end_time = time.perf_counter()
elapsed_time = end_time - start_time
elapsed_time = elapsed_time / 60  # Convertir a minutos
print("Total de simulaciones con error:", total_simulations_error, "de", total_simulations)
print(f'Tiempo total transcurrido: {elapsed_time:.2f} minutos')
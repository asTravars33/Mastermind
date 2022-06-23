import random
import sys

BLACK_WT = 2
WHITE_WT = 5
CONST_WT = 2 
POPULATION_SIZE = 60
NUM_CLONES = 10
TOURNAMENT_SIZE = 15
TOURNAMENT_WIN_PROBABILITY = 0.75
MUTATION_RATE = 0.2
NUM_TRIALS = 5 
NUM_COLORS = 4
COLOR_POOL = ["Y", "B", "G", "P", "O", "R"]
RADIUS = 2

def update(dict, key, val):
    if not key in dict:
        dict[key] = val
    else:
        dict[key] = dict[key]+val 
        if dict[key]==0:
            dict.pop(key) 
def score(guess, targ):
    #print(guess, targ) 
    num_black = 0
    num_white = 0
    # Make frequency map of the answer code 
    freqs = {}
    for let in targ:
        update(freqs, let, 1) 
    # Go through the guess
    for i in range(len(guess)):
        if guess[i]==targ[i]:
            num_black = num_black + 1 
            update(freqs, guess[i], -1) 
    for i in range(len(guess)):
        if guess[i]!=targ[i] and guess[i] in freqs:
            num_white = num_white + 1
            update(freqs, guess[i], -1) 
    return num_black, num_white 
def eligibility(code, prev_guesses):
    sum_diff_black = 0
    sum_diff_white = 0
    for guess, info in prev_guesses:
        # Determine the output if "guess" was the correct one
        num_black, num_white = score(code, guess) 
        sum_diff_black = sum_diff_black + abs(num_black-info[0])
        sum_diff_white = sum_diff_white + abs(num_white-info[1]) 
    return sum_diff_black, sum_diff_white 
def is_eligible(code, prev_guesses):
    diff_black, diff_white = eligibility(code, prev_guesses)
    return diff_black==0 and diff_white==0 
def fitness(code, prev_guesses):
    diff_black, diff_white = eligibility(code, prev_guesses) 
    return BLACK_WT*diff_black + WHITE_WT*diff_white
    #return BLACK_WT*diff_black + WHITE_WT*diff_white + CONST_WT*NUM_COLORS*(played_turns-1) 
def sim_score(code, eligible_codes):
    sum = 0
    for oth in eligible_codes:
        if oth==code:
            continue 
        num_black, num_white = score(code, oth) 
        sum = sum + BLACK_WT*num_black + WHITE_WT*num_white 
    # Higher sum = more similar to other eligible codes 
    return sum 

## Genetic algorithm helpers 
def rand_code():
    code = ""
    for i in range(NUM_COLORS):
        code = code + random.choice(COLOR_POOL)
    return code 
def next_generation(population, to_clone, code_scores, prev_guesses):
    next_gen = {elem for elem in to_clone}
    #print("AAA PREV GUESSES:", prev_guesses) 
    while len(next_gen)<POPULATION_SIZE:
        child = tournament_selection(population, code_scores) 
        if not child in next_gen:
            next_gen.add(child) 
    return list(next_gen), {code for code in next_gen if is_eligible(code, prev_guesses)}
def tournament_selection(population, code_scores):
    # Make both groups
    selected = random.sample(population, 2*TOURNAMENT_SIZE)
    group1 = selected[:TOURNAMENT_SIZE]
    group2 = selected[TOURNAMENT_SIZE:]
    # Determine winners
    winner1 = winner(group1, code_scores) 
    winner2 = winner(group2, code_scores) 
    # Return a new child
    return breed(winner1, winner2) 
def winner(group, code_scores):
    # Sort group based on sim_score
    def val(code):
        return code_scores[code] 
    group = sorted(group, key = val, reverse=True)
    # Select the winner using prob rate
    for i in range(len(group)):
        if i==len(group)-1 or random.random()<TOURNAMENT_WIN_PROBABILITY:
            return group[i]
def breed(strategy1, strategy2): 
    parent1 = strategy1 if random.random()<0.5 else strategy2
    parent2 = strategy2 if parent1==strategy1 else strategy1 
    child = ""
    # Do crossover
    num_cross = random.randint(1, len(parent1)-1)
    cross_idx = random.sample([i for i in range(len(parent1))], num_cross)
    for i in range(NUM_COLORS):
        if i in cross_idx:
            child = child + parent1[i]
        else:
            child = child+ parent2[i]
    # Mutate?
    if random.random()<MUTATION_RATE:
        to_swap = random.sample([i for i in range(len(child))], 2) 
        idx1, idx2 = min(to_swap[0], to_swap[1]), max(to_swap[0], to_swap[1])
        temp = child[idx1] 
        child = child[:idx1]+child[idx2]+child[idx1+1:idx2]+temp+child[idx2+1:]
    return child 
def next_guess(prev_guesses, gen_lim):
    ## Initial population + code guess 
    population = [rand_code() for i in range(POPULATION_SIZE)]
    final_codes = {code for code in population if is_eligible(code, prev_guesses)}
    gen_num = 1
    # Genetic algorithm 
    while gen_num<gen_lim and len(final_codes)<POPULATION_SIZE:
        # Make the next population 
        code_scores = {code: fitness(code, prev_guesses) for code in population}
        def val(code):
            return code_scores[code]
        codes_sorted = sorted(population, key=val) 
        info = next_generation(population, codes_sorted[:NUM_CLONES], code_scores, prev_guesses) 
        population = info[0]
        cur_eligible = info[1]
        for eligible in cur_eligible:
            if not eligible in final_codes:
                final_codes.add(eligible) 
        gen_num = gen_num + 1
    if len(final_codes)==0:
        return None 
    return get_guess(final_codes) 
def run_game(prev_guesses_str):
    prev_guesses = str_to_prev_guesses(prev_guesses_str)
    ## Generate the next population
    POPULATION_SIZE = 60
    guess = next_guess(prev_guesses, 10)
    if guess==None:
        POPULATION_SIZE = 150
        while guess==None:
            guess = next_guess(prev_guesses, 5)
    return guess
def get_guess(eligible):
    max_sim = -10000
    best_code = ""
    for code in eligible:
        score = sim_score(code, eligible) 
        #print("score:", score) 
        if score>max_sim:
            max_sim = score
            best_code = code 
    return best_code 
def str_to_prev_guesses(str):
    print("str", str)
    info = str.split(";")
    prev_guesses = []
    for i in range(len(info)-1):
        entry = info[i]
        cur_info = entry.split("-")
        prev_guesses.append((cur_info[0], (int(cur_info[1]), int(cur_info[2]))))
    print(prev_guesses)
    return prev_guesses

# List of (guess, (num_black, num_white))
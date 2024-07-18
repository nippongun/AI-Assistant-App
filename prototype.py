import requests
import json
from openai import OpenAI

def generate_prompts(name,location, news_categories, stocks, calendar=None):
    prompt = f"Generate daily prompt for {name}  located in {location}. Include:\n"
    prompt += f"1. Weather forecast for {location}\n"
    prompt += f"2. Top news headlines in categories: {','.join(news_categories)}\n"
    prompt += f"3. Stock prices for {','.join(stocks)}\n"

    if calendar:
        prompt += "4. Calendar events for {calendar}\n"

    prompt += "Provide a concise summary for each section."

    return prompt

def generate_all_prompts(file_path):
    prompts_data = load_prompts_from_json(file_path)
    generated_prompts = []

    for prompt_info in prompts_data:
        name = prompt_info.get("name")
        location = prompt_info.get("location")
        news_categories = prompt_info.get("news_categories")
        stocks = prompt_info.get("stocks")
        calendar = prompt_info.get("calendar")

        prompt = generate_prompts(name, location, news_categories, stocks, calendar)
        generated_prompts.append(prompt)
    
    return generated_prompts

def load_prompts_from_json(file_path):
    try:
        with open(file_path, 'r') as f:
            data = json.load(f)

            if "prompts" in data and isinstance(data["prompts"], list):
                print(data["prompts"])
                return data["prompts"]
    
    except json.JSONDecodeError:
        print("Error: Invalid JSON format in the file.")
        return []
    except FileNotFoundError:
        print(f"Error: File '{file_path}' not found.")
        return []
    except Exception as e:
        print(f"An unexpected error occurred: {str(e)}")
        return []

def get_api_key():
    with open("config.json") as f:
        config = json.load(f)

    return (config["organization"], config["project"])

def get_ai_response(prompt):

    client = OpenAI(organization=get_api_key()[0], project= get_api_key()[1])

    completion = client.chat.completions.create(
        model="gpt-3.5-turbo",
        messages=[
            {"role": "user", "content": prompt},
            {"role": "system", "content": "You are a helpful assistant."}
        ]
    )
    print(completion.choices[0].message)

def main():
    all_prompts = generate_all_prompts("prompts.json")

    if all_prompts.count == 0:
        print("Error: No prompts found in the file.")
        return

    text = all_prompts[0]
    print(text)
    get_ai_response(text)

main()
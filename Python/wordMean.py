from flask import Flask, request, jsonify
from flask_cors import CORS
import google.generativeai as genai
import json


# Google Generative AI API 설정
genai.configure(api_key="")
model = genai.GenerativeModel('gemini-1.5-flash')

app = Flask(__name__)
CORS(app)

@app.route('/Python/word-mean', methods=['POST'])
def word_mean():
    data = request.get_json()
    word = data.get('word')
    if not word:
        return jsonify({"error": "word parameter is missing"}), 400  # 오류 응답

    # Google Generative AI API 호출
    try:
        response = model.generate_content(
            f"{word} 을 word: {word} partofspeech:품사 mean:한국어 뜻 speech:발음기호 example:생성한 예문(예문의 뜻) (단,예문은 1개) 형태로 json 형식으로 보내시오."
        )
        response_content = response.text

        # JSON 데이터 추출
        json_start = response_content.find("{")
        json_end = response_content.rfind("}")
        if json_start == -1 or json_end == -1:
            return jsonify({"error": "JSON data not found in AI response"}), 500  # 오류 응답

        json_text = response_content[json_start:json_end + 1]
        
        print(json_text)

        # JSON 텍스트를 파싱
        parsed_json = json.loads(json_text)

        # 파싱된 데이터를 파일로 저장 (옵션)
        with open("word_mean.json", "w", encoding="utf-8") as json_file:
            json.dump(parsed_json, json_file, ensure_ascii=False, indent=4)

        # 클라이언트로 JSON 데이터 반환
        return jsonify(parsed_json)

    except json.JSONDecodeError as e:
        return jsonify({"error": "Failed to parse JSON", "details": str(e)}), 500

    except Exception as e:
        return jsonify({"error": "An unexpected error occurred", "details": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
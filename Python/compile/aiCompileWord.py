from flask import Flask, request, jsonify 
from flask_cors import CORS
from bs4 import BeautifulSoup
import requests
import pytesseract
from PIL import Image
import fitz 
import docx
import os
import io
import google.generativeai as genai # Google Gemini API
from config import Config

app = Flask(__name__)
CORS(app)

# Tesseract 경로 설정
pytesseract.pytesseract.tesseract_cmd = r"/opt/anaconda3/envs/pydatavenv/bin/tesseract"

# Google Gemini API 설정
genai.configure(api_key=Config.GOOGLE_API_KEY)
system_instruction = (
                        f"단어를 json으로 만들 때 관사, be 동사, 인칭 대명사, 지시 대명사, 고유 명사(사람 이름, 회사 이름 등)는 제외해주세요."
                        f"중복된 단어는 무조건 꼭 제거하고, 최소 1개에서 최대 200개의 단어를 소문자로 보여주세요."
                        f"jsonArray중 json데이터의 key는 ""단어"", ""뜻"" 한개만 오고, key가 ""단어""인 value에는 영어단어가, key가 ""뜻""인 value에는 품사와 단어뜻을 넣어주세요."
                        f"뜻은 무조건 한국어입니다 뜻 앞에 품사의 영어약어도 함께 넣어주세요., json데이터의 key도 한글로 작성해 주세요"
                        f"결과에는 어떠한 말도 넣지 말고 json데이터만 반환해 주세요"
                    ) 
model = genai.GenerativeModel('gemini-1.5-flash',system_instruction=system_instruction)


@app.route('/apiCompile', methods=['POST'])
def compile_word():
    print("--python--")
    try:
        # [1] 요청 데이터 처리
        data = request.json
        compile_source = data.get("compileSource")  # URL 또는 텍스트 데이터
        original_files = data.get("originalFiles", [])  # 파일 데이터 리스트
        print(f"파일 유무 확인 : {original_files}")
        base_url = "http://localhost:8081"  # Spring 서버 주소

        file_data = ""  # 파일 처리 결과 저장
        source_type = ""  # 데이터 타입 지정

        # [2] 파일 유무 및 소스 타입 처리
        if original_files:  # 파일이 있는 경우
            print("파일이 업로드되었습니다.")
            file_data = process_uploaded_files(original_files, base_url)
            print(f"file_data : {file_data}") 

            if compile_source:  # 경우 1 & 2: 파일 유, compileSource 존재
                if is_url(compile_source):
                    print("경우 1: 파일 유, compileSource = URL")
                    text_data = extract_text_from_url(compile_source)
                else:
                    print("경우 2: 파일 유, compileSource = 텍스트")
                    text_data = compile_source
                    
            else:  # 경우 5: 파일 유, compileSource 무
                print("경우 5: 파일 유, compileSource 무")
                text_data=""
                
            print(f"file_data : {file_data}")    
            print(f"text_data : {text_data}") 
            
        else:  # 파일이 없는 경우
            print("파일이 업로드되지 않았습니다.")
            file_data = ""
            if compile_source:  # 경우 3 & 4: 파일 무, compileSource 존재
                if is_url(compile_source):
                    print("경우 3: 파일 무, compileSource = URL")
                    text_data = extract_text_from_url(compile_source)
                else:
                    print("경우 4: 파일 무, compileSource = 텍스트")
                    text_data = compile_source
                    
            else:
                print("파일 소스 읽어오지 못함")
                return jsonify({"error": "파일과 소스 데이터가 모두 제공되지 않았습니다."}), 400

            print(f"file_data : {file_data}")    
            print(f"text_data : {text_data}")
        # [3] AI 요청 데이터 생성
        line = (
            f"아래 데이터1과 데이터2에서 문장이 있다면 문맥을 분석해 단어를 영어 단어 단위로 나눠주세요."
            f"단어의 뜻은 문맥상 적합한 뜻을 우선으로 넣고, 문맥이 없다면 일반적으로 사용 빈도가 높은 뜻을 작성하세요."
            f"csv파일로 저장하기 쉬운 json데이터가 필요하고, csv헤더로 ""단어"",""뜻""이 올거에요."
            f"데이터1 : {file_data}\n"
            f"데이터2 : {text_data}"
        )
        
        # 한단어당 품사array(여러개의 품사), 한개의 품사당 뜻이 2개 (뜻1: 한국어뜻1, 뜻2: 한국어뜻2)  
        print(f"===========[요청 사항]=============")
        print(f"{line}")
        # [4] AI API 호출
        print("================[응답 결과]================")
        response = send_text_to_ai(line)
        print(f"ai 결과 : {response}")
        print("================================")

        # [5] 응답 반환
        return jsonify(response)

    except Exception as e:
        return jsonify({"error": "처리 중 오류 발생", "details": str(e)}), 500


def is_url(string):
    print("입력된 문자열이 URL인지 확인하는 함수")
    return string.startswith("http://") or string.startswith("https://")


def process_uploaded_files(files, base_url):

    print("업로드된 파일을 처리 - process_uploaded_files")
    
    folder_path = "../compileFiles"
    DeleteAllFiles(folder_path)
    
    file_results = []
    for file_info in files:
        print(f"files처리 for문 {files}")
        file_path = file_info.get("filePath")
        file_name = file_info.get("fileName")
        
        # file_path가 리스트인 경우 문자열로 변환
        if isinstance(file_path, list):
            file_path = "/".join(file_path)  # 리스트를 문자열로 변환
        
        file_path_parts = file_path.split("static",1)
        if len(file_path_parts) > 1:
        # uploads 이후의 문자열 (앞에 '/'가 있다면 그대로 사용)
            file_path_str = file_path_parts[1]
        else:
            file_path_str = file_path_parts[0]
            
        download_url = f"{base_url}{file_path_str}/{file_name}"
        print(f"processfiles-{download_url}")
        try:
            # 파일 다운로드
            response = requests.get(download_url, stream=True)
            print(f"downfiles-{response}")
            response.raise_for_status()

            local_file_path = f"../compileFiles/{file_name}"
            print(f"file_paht : {local_file_path}")
            with open(local_file_path, "wb") as file:
                for chunk in response.iter_content(chunk_size=8192):
                    print("여기까지")
                    file.write(chunk)

            # 파일 내용 추출
            extracted_text = process_file(local_file_path)
            print(f"extracted_text 리턴값 : {extracted_text}")
            file_results.append({"text": extracted_text})

        except requests.exceptions.RequestException as e:
            print("오류")
            file_results.append({"error": str(e)})

    return file_results


def process_file(file_path):
    print("파일 유형에 따라 적절한 처리 함수 호출")
    
    print("process_file")
    file_name,file_extension = os.path.splitext(file_path)
    print(f"file_name : {file_name} / file_extension1 : {file_extension}")
    file_extension = file_extension.lower()
    print(f"file_extension2 : {file_extension}")

    if file_extension in [".jpg", ".jpeg", ".png"]:
        print("이미지에서 OCR로 텍스트 추출")
        image = Image.open(file_path)
        return pytesseract.image_to_string(image, lang='eng')
        

    elif file_extension == ".pdf":
        print("pdf 파일에서 텍스트 추출")
        return process_pdf_with_ocr(file_path)
        
    elif file_extension == ".docx":
         print("Word 파일에서 텍스트 추출")
         doc = docx.Document(file_path)
         return "\n".join([paragraph.text for paragraph in doc.paragraphs])
        
    elif file_extension == ".txt":
         print("텍스트 파일에서 텍스트 추출")
         with open(file_path, "r", encoding="utf-8") as file:
            return file.read()

    else:
        return f"지원되지 않는 파일 형식: {file_extension}"

def process_pdf_with_ocr(pdf_path):
    print("PDF에서 텍스트 또는 이미지를 OCR로 처리")
    pdf_text = ""
    with fitz.open(pdf_path) as pdf:
        for page in pdf:
            images = page.get_images(full=True)
            if images:
                for img in images:
                    xref = img[0]
                    base_image = pdf.extract_image(xref)
                    image_bytes = base_image["image"]
                    image = Image.open(io.BytesIO(image_bytes))
                    pdf_text += pytesseract.image_to_string(image, lang="eng")
            else:
                pdf_text += page.get_text()
    return pdf_text

def send_text_to_ai(line):
    print("Google Gemini API를 호출하여 단어장 생성")
    
    try:
        response = model.generate_content(line)
        print(response)
        return {"status": "success", "response": response.to_dict()}
    except Exception as e:
        return {"error": f"AI 처리 중 오류 발생: {str(e)}"}

def extract_text_from_url(url):
    try:
        # [1] URL에 GET 요청
        response = requests.get(url)
        response.raise_for_status()  # 요청 실패 시 예외 발생

        # [2] HTML 파싱
        soup = BeautifulSoup(response.text, 'lxml')

        # [3] 텍스트 추출
        # 'body' 태그 내의 모든 텍스트를 추출
        page_text = soup.body.get_text(separator="\n", strip=True)

        return page_text
    except requests.exceptions.RequestException as e:
        return f"요청 중 오류 발생: {str(e)}"
    except Exception as e:
        return f"처리 중 오류 발생: {str(e)}"
    
def DeleteAllFiles(filePath):
    if os.path.exists(filePath):
        for file in os.scandir(filePath):
            os.remove(file.path)
        

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)

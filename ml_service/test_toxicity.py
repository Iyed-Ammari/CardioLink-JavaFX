from transformers import pipeline

def test_toxicity():
    print("Loading model...")
    toxicity_pipeline = pipeline("text-classification", model="unitary/multilingual-toxic-xlm-roberta", top_k=None)
    
    test_texts = ["fuck", "putain", "bonjour"]
    
    for text in test_texts:
        print(f"\nText: {text}")
        results = toxicity_pipeline(text)
        print(results)

if __name__ == "__main__":
    test_toxicity()

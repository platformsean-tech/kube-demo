package main

import (
	"encoding/json"
	"log"
	"net/http"
	"sync"
	"time"

	"github.com/google/uuid"
)

type Player struct {
	ID        string    `json:"id"`
	Username  string    `json:"username"`
	Balance   int64     `json:"balance"`
	CreatedAt time.Time `json:"created_at"`
}

var (
	players = make(map[string]Player)
	mu      sync.RWMutex
)

func createPlayer(w http.ResponseWriter, r *http.Request) {
	start := time.Now()
	if r.Method != http.MethodPost {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}
	var req struct {
		Username string `json:"username"`
	}

	err := json.NewDecoder(r.Body).Decode(&req)
	if err != nil {
		http.Error(w, "invalid json", http.StatusBadRequest)
		return
	}
	if req.Username == "" {
		http.Error(w, "invalid json", http.StatusBadRequest)
		return
	}
	player := Player{
		ID:        uuid.NewString(),
		Username:  req.Username,
		Balance:   0,
		CreatedAt: time.Now(),
	}

	mu.Lock()
	players[player.ID] = player
	mu.Unlock()

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(player)

	log.Printf(
		"CREATE player id=%s username=%s duration=%s",
		player.ID,
		player.Username,
		time.Since(start),
	)

}

func getPlayer(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}
	id := r.URL.Query().Get("id")
	if id == "" {
		http.Error(w, "ID required", http.StatusBadRequest)
		return
	}
	mu.RLock()
	player, ok := players[id]
	mu.RUnlock()
	if !ok {
		http.Error(w, "Player not found", http.StatusNotFound)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(player)

}
func main() {
	mux := http.NewServeMux()

	mux.HandleFunc("/create", createPlayer)
	mux.HandleFunc("/get", getPlayer)

	log.Println("Starting server on :8080")
	log.Fatal(http.ListenAndServe(":8080", mux))

}

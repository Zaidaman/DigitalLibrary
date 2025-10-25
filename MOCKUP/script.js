document.addEventListener('DOMContentLoaded', function() {
    // Elementi DOM per la gestione dei libri
    const addButton = document.getElementById('addButton');
    const addBookForm = document.getElementById('addBookForm');
    
    // Elementi DOM per la gestione delle librerie
    const addLibraryBtn = document.getElementById('addLibraryBtn');
    const libraryForm = document.getElementById('libraryForm');
    const newLibraryForm = document.getElementById('newLibraryForm');
    const cancelLibraryBtn = document.getElementById('cancelLibrary');
    const libraryList = document.getElementById('libraryList');

    // Gestione del form dei libri
    addButton.addEventListener('click', function() {
        addBookForm.classList.add('active');
    });

    addBookForm.addEventListener('click', function(e) {
        if (e.target === addBookForm) {
            addBookForm.classList.remove('active');
        }
    });

    // Carica le librerie salvate dal localStorage
    loadLibraries();

    // Gestione del form delle librerie
    addLibraryBtn.addEventListener('click', function() {
        libraryForm.classList.add('active');
    });

    cancelLibraryBtn.addEventListener('click', function() {
        libraryForm.classList.remove('active');
        newLibraryForm.reset();
    });

    newLibraryForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const libraryName = document.getElementById('libraryName').value.trim();
        
        if (libraryName) {
            addLibrary(libraryName);
            libraryForm.classList.remove('active');
            newLibraryForm.reset();
        }
    });

    libraryForm.addEventListener('click', function(e) {
        if (e.target === libraryForm) {
            libraryForm.classList.remove('active');
            newLibraryForm.reset();
        }
    });

    function addLibrary(name) {
        const libraryItem = document.createElement('div');
        libraryItem.className = 'library-item';
        
        // Crea l'header della libreria con i controlli
        const libraryHeader = document.createElement('div');
        libraryHeader.className = 'library-header';
        libraryHeader.innerHTML = `
            <span class="library-name">${name}</span>
            <div class="library-controls">
                <button class="toggle-library" title="Espandi/Comprimi">
                    <i class="fas fa-chevron-down"></i>
                </button>
                <button class="delete-library" title="Elimina libreria">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;

        // Crea il contenuto della libreria
        const libraryContent = document.createElement('div');
        libraryContent.className = 'library-content';
        libraryContent.innerHTML = `
            <div class="library-empty">
                Nessun libro in questa libreria
            </div>
        `;

        // Aggiungi l'header e il contenuto al container della libreria
        libraryItem.appendChild(libraryHeader);
        libraryItem.appendChild(libraryContent);

        // Gestione del toggle per espandere/comprimere
        const toggleBtn = libraryHeader.querySelector('.toggle-library');
        toggleBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            const content = this.closest('.library-item').querySelector('.library-content');
            const icon = this.querySelector('i');
            
            content.classList.toggle('active');
            if (content.classList.contains('active')) {
                icon.className = 'fas fa-chevron-up';
            } else {
                icon.className = 'fas fa-chevron-down';
            }
            saveLibraries(); // Salva lo stato dopo il toggle
        });

        // Gestione del click sull'header per espandere/comprimere
        libraryHeader.addEventListener('click', function() {
            const content = this.nextElementSibling;
            const icon = this.querySelector('.toggle-library i');
            
            content.classList.toggle('active');
            if (content.classList.contains('active')) {
                icon.className = 'fas fa-chevron-up';
            } else {
                icon.className = 'fas fa-chevron-down';
            }
            saveLibraries(); // Salva lo stato dopo il toggle
        });

        // Gestione dell'eliminazione
        const deleteBtn = libraryHeader.querySelector('.delete-library');
        deleteBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            if (confirm('Sei sicuro di voler eliminare questa libreria?')) {
                libraryItem.remove();
                saveLibraries();
            }
        });

        libraryList.appendChild(libraryItem);
        saveLibraries();
    }

    function saveLibraries() {
        const libraries = [];
        document.querySelectorAll('.library-item').forEach(item => {
            libraries.push({
                name: item.querySelector('.library-name').textContent,
                isExpanded: item.querySelector('.library-content').classList.contains('active')
            });
        });
        localStorage.setItem('libraries', JSON.stringify(libraries));
    }

    function loadLibraries() {
        const libraries = JSON.parse(localStorage.getItem('libraries')) || [];
        libraries.forEach(library => {
            addLibrary(library.name);
            if (library.isExpanded) {
                const lastAdded = libraryList.lastElementChild;
                const content = lastAdded.querySelector('.library-content');
                const icon = lastAdded.querySelector('.toggle-library i');
                content.classList.add('active');
                icon.className = 'fas fa-chevron-up';
            }
        });
    }
});
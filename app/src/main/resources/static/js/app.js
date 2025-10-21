document.addEventListener('DOMContentLoaded', () => {
  if (!window.Chart) return;

  const detCanvas = document.getElementById('chartDetections');
  if (detCanvas) {
    new Chart(detCanvas.getContext('2d'), {
      type: 'line',
      data: {
        labels: Array.from({length: 24}, (_, i) => `${i}:00`),
        datasets: [{label: 'Detections', data: Array.from({length:24},()=>Math.round(20+Math.random()*30))}]
      },
      options: {responsive: true, maintainAspectRatio: false}
    });
  }

  const turbCanvas = document.getElementById('chartTurbidity');
  if (turbCanvas) {
    new Chart(turbCanvas.getContext('2d'), {
      type: 'line',
      data: {
        labels: Array.from({length: 24}, (_, i) => `${i}:00`),
        datasets: [{label: 'NTU', data: Array.from({length:24},()=>+(10+Math.random()*15).toFixed(2))}]
      },
      options: {responsive: true, maintainAspectRatio: false}
    });
  }
});
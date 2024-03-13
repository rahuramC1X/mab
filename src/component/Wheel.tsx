import { Dialog } from 'primereact/dialog';
import React, { useEffect, useState } from 'react';
import './MyRouletteComponent.css';

interface WheelData {
  mustSpin: boolean;
  prizeNumber: number | null;
  divisions: Division[];
}

interface Division {
  id: number;
  number: number;
  probability: number;
  selected: boolean;
  color: string;
}

// let count = 0;
// const createWebSocketConnection = ()=> {
//   const socket = new WebSocket('ws://localhost:8080/websocket');
//   socket.onopen = () => {
//     count = count+1
//     console.log('WebSocket connected'+ count);
//   };
//   return socket;
// }
const socket = new WebSocket('ws://localhost:8080/websocket')
  

  


  
socket.onclose = () => {
  console.log('WebSocket connection closed');
  };
  
  socket.onerror = (error) => {
  console.error('WebSocket connection error:', error);
  };

  let totalSpinCount = 0

const MyRouletteComponent: React.FC = () => {
  
  const [wheels, setWheels] = useState<WheelData[]>([]);
  const [messages, setMessages] = useState<string[]>([]);
  const [spinCounts, setSpinCounts] = useState<number[]>(Array.from({ length: 10 }, () => 0)); // Array to track spin counts
  const [clickedWheel, setClickedWheel] = useState<number | null>(null); // Track which wheel has been clicked
  const [imageData, setImageData] = useState<string |null>(null)
  const [showPopup, setShowPopup] = useState<boolean>(false)

  // Function to generate divisions with random numbers and weighted probabilities
  const generateDivisionsWithProbabilities = (probabilities: number[]): Division[] => {
    const divisions: Division[] = [];
    const colors = ['#F5F5DC', '#F4A460']; // Black and grey colors for divisions
  
    // Create an array of objects containing both number and probability
    const data: { number: number, probability: number }[] = [];
    for (let i = 0; i < probabilities.length; i++) {
      data.push({ number: i + 1, probability: probabilities[i] });
    }
  
    // Shuffle the data array
    for (let i = data.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [data[i], data[j]] = [data[j], data[i]];
    }
  
    // Create divisions with shuffled values and probabilities
    for (let i = 0; i < data.length; i++) {
      divisions.push({ id: i, number: data[i].number, probability: data[i].probability, selected: false, color: colors[i % colors.length] });
    }
  
    return divisions;
  };

  
  const generateRandomProbabilities = (count: number): number[] => {
    const probabilities: number[] = [];
    const mean = 0.5; // Mean value for the normal distribution
    const variance = 0.5; // Variance for the normal distribution
  
    for (let i = 0; i < count; i++) {
      const probability = Math.max(0, Math.min(1, mean + (Math.random() * variance * 2 - variance))); // Ensure probability is within [0, 1] range
      probabilities.push(probability);
    }
  
    // Normalize probabilities so that they sum up to 1
    const total = probabilities.reduce((acc, val) => acc + val, 0);
    for (let i = 0; i < probabilities.length; i++) {
      probabilities[i] /= total;
    }
  
    return probabilities;
  };
  const calculatePrizeNumber = (divisions: Division[]): number => {
    const totalWeight = divisions.reduce((sum, division) => sum + division.probability, 0);
    let random = Math.random() * totalWeight;
    for (let i = 0; i < divisions.length; i++) {
      random -= divisions[i].probability;
      if (random <= 0) {
        return i;
      }
    }
    return divisions.length - 1; // Fallback
  };
  

  // Function to handle spin click for a particular wheel
  // Function to handle spin click for a particular wheel
const handleSpinClick = (wheelIndex: number) => {
  const selectedWheel = wheels[wheelIndex];
  if (!selectedWheel || !selectedWheel.divisions) {
    // Wheel data not available yet, return early
    return;
  }

  setClickedWheel(wheelIndex); // Set the clicked wheel index  
  const newSpinCounts = [...spinCounts]; // Create a copy of the spin counts array
  newSpinCounts[wheelIndex] += 1; // Increment spin count for the clicked wheel
  setSpinCounts(newSpinCounts);

  const newWheels = wheels.map((wheel, index) => {
    if (index === wheelIndex) {
      const prizeNumber = calculatePrizeNumber(wheel.divisions);
      
        const message = { index: wheelIndex, value: wheel.divisions[prizeNumber].number, frequency: newSpinCounts[wheelIndex], totalSpinCount: totalSpinCount };
        const jsonMessage = JSON.stringify(message);
        socket.send(jsonMessage); // Send message to WebSocket after spinning
        setTimeout(() => {
          setWheels(prevWheels => prevWheels.map((prevWheel, idx) => idx === index ? { ...prevWheel, prizeNumber, mustSpin: false } : prevWheel));
        },1000)
      return {
        ...wheel,
        prizeNumber: null, // Clear previous result
        mustSpin: true,
      };
    }
    return wheel;
  });
  console.log(newWheels)
  setTimeout(() => {
    setClickedWheel(null);
  }, 500);
  setWheels(newWheels);
};

  
  


  // Function to log the probabilities and values for each wheel
  const logWheelData = () => {
    wheels.forEach((wheel, index) => {
      console.log("Probability " + wheel.divisions.map(division => division.probability));
      console.log("Numbers  " + wheel.divisions.map(division => division.number));
    });
  };

  // Initialize the wheels with default data and random probabilities
  useEffect(() => {
    const initialWheels: WheelData[] = Array.from({ length: 10 }, (_, index) => ({
      mustSpin: false,
      prizeNumber: null,
      divisions: generateDivisionsWithProbabilities(generateRandomProbabilities(10)), // Example: Generate random probabilities for each wheel
    }));
    setWheels(initialWheels);

    
  }, []);

  const handleClosePopup = () => {
    setShowPopup(false);
    setImageData(null);
  };

  


  socket.onmessage = (event) => {
    const message = event.data;
    console.log('Received message from server:', message);
    if(message === "start")
      {setShowPopup(false)}
    if(message === "end" || message === "start"){
       totalSpinCount = 0
       const zero_array = new Array(10).fill(0)
       setSpinCounts(zero_array)
    }
    else 
      if (message.startsWith('Image')) {
        console.log("entered")
        // Handle the image message
        const jsonPart = message.substring('Image'.length);
        try {
          const parsedMessage = JSON.parse(jsonPart); // Parse the JSON message
          if (parsedMessage && parsedMessage.image) {
            // Extract the base64Image from the parsed message
            const base64Image = parsedMessage.image;
    
            // Display the image using an <img> element
            // Assuming you have a state variable to track the received image data
            setImageData(base64Image); // Update the state with the image data
            setShowPopup(true); // Show
          }
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
    }
    else {
      const wheelIdToSpin = parseInt(message) - 1
      if (!isNaN(wheelIdToSpin)) {
        totalSpinCount += 1
        handleSpinClick(wheelIdToSpin);
      }
    };
    
    };

    socket.onopen = () => {
      console.log('WebSocket opened');
      for (let i = 0; i < 10; i++) {
         const message = { index: i, value: 0, frequency: 0, totalSpinCount: 0 };
         const jsonMessage = JSON.stringify(message);
         socket.send(jsonMessage);
      }
    };
  useEffect(() => {
    logWheelData();
   

  }, []);
  
  return (
    <div className="roulette-container">
      <h1 className="roulette-heading">Multi Armed Roulette Wheel</h1>
      <div className='wheels-container'>
      {wheels.map((wheel, index) => (
        <div key={index} className="wheel-bg" >
          <div key={index} className="wheel-container" onClick={() => handleSpinClick(index)}  >
          
          <svg className={`wheel ${wheel.mustSpin ? 'spin' : ''}`} viewBox="0 0 300 300">
            <circle cx="150" cy="150" r="140" fill="#ffd700" stroke='white' strokeWidth="15" />
            {wheel.divisions.map((division, idx) => (
              <g key={idx}>
                <path d={`M 150,150 L ${150 + 140 * Math.cos((idx / wheel.divisions.length) * Math.PI * 2)},${150 + 140 * Math.sin((idx / wheel.divisions.length) * Math.PI * 2)} A140,140 0 ${(1) % 10 === 0 ? '1' : '0'} 1 ${150 + 140 * Math.cos(((idx + 1) / wheel.divisions.length) * Math.PI * 2)},${150 + 140 * Math.sin(((idx + 1) / wheel.divisions.length) * Math.PI * 2)} Z`} fill={division.color} />
                <text x={150 + 100 * Math.cos(((idx + 0.5) / 10) * Math.PI * 2)} y={150 + 100 * Math.sin(((idx + 0.5) / 10) * Math.PI * 2)} textAnchor="middle" dominantBaseline="middle" fill="black" fontWeight="bold">₹ {(division.number * 100)}</text>
              </g>
            ))}
            {wheel.prizeNumber !== null && (
              <line
                x1="150"
                y1="150"
                x2={150 + 55 * Math.cos(((wheel.prizeNumber + 0.5) / 10) * Math.PI * 2)}
                y2={150 + 55 * Math.sin(((wheel.prizeNumber + 0.5) / 10) * Math.PI * 2)}
                stroke="red"
                strokeWidth="6"
                strokeLinecap="round"
              />
            )}
            {wheel.prizeNumber !== null && (
              <g className={`result-animation ${wheel.prizeNumber != null ? 'animate-result' : ''}`}>
                <text x="150" y="150" textAnchor="middle" dominantBaseline="middle" className="funky-font" style={{ fill: 'purple', fontSize: '42px' }}>
                 ₹ {wheel.divisions[wheel.prizeNumber].number * 100}
                </text>
              </g>
            )}
             

          </svg>

          
          
           
          </div>
          {clickedWheel === index && (
              <div className={`hammer ${clickedWheel === index ? 'clicked' : ''}`}></div>
            )}
        </div>
      ))}
      </div>
      {imageData && (
         <Dialog header="Image Popup" visible={showPopup} onHide={handleClosePopup}>
           <img src={`data:image/jpeg;base64,${imageData}`} alt="Received Image" />
       </Dialog>
      )}
    </div>
  );
};

export default MyRouletteComponent;